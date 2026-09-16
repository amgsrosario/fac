from pathlib import Path
import csv
import re

import pdfplumber


BASE_DIR = Path(__file__).resolve().parent
PDF_PATH = BASE_DIR / "sources" / "TabelaFreguesias (1).pdf"
CSV_PATH = BASE_DIR / "freguesias_portugal.csv"


LINE_RE = re.compile(
    r"^(?P<concelho>.+?)\s+"
    r"(?P<distrito>\d{2})\s+"
    r"(?P<concelho_codigo>\d{2})\s+"
    r"(?P<freguesia_codigo>[A-Z0-9]{2})\s+"
    r"(?P<nome>.+)$"
)


def normalizar_linha(texto: str) -> str:
    return " ".join(texto.split()).strip()


def main():
    if not PDF_PATH.exists():
        raise FileNotFoundError(f"PDF não encontrado: {PDF_PATH}")

    registos = []
    ignoradas = []

    with pdfplumber.open(PDF_PATH) as pdf:
        for page_num, page in enumerate(pdf.pages, start=1):
            texto = page.extract_text() or ""

            for raw_line in texto.splitlines():
                linha = normalizar_linha(raw_line)

                if not linha:
                    continue

                if linha == "CONCELHO FREGUESIA":
                    continue

                if linha.startswith("TABELA DE FREGUESIAS"):
                    continue

                if linha.startswith("(Atualizado "):
                    continue

                match = LINE_RE.match(linha)

                if not match:
                    ignoradas.append((page_num, linha))
                    continue

                dados = match.groupdict()

                nome = dados["nome"]
                extinta = nome.endswith("(EXTINTA)")

                if extinta:
                    nome = nome.removesuffix("(EXTINTA)").strip()

                codigo = (
                    f"{dados['distrito']}"
                    f"{dados['concelho_codigo']}"
                    f"{dados['freguesia_codigo']}"
                )

                registos.append(
                    {
                        "codigo": codigo,
                        "codigo_distrito": dados["distrito"],
                        "codigo_concelho": dados["concelho_codigo"],
                        "codigo_freguesia": dados["freguesia_codigo"],
                        "concelho": dados["concelho"],
                        "nome": nome,
                        "extinta": "true" if extinta else "false",
                    }
                )

    codigos = [r["codigo"] for r in registos]
    duplicados = sorted({c for c in codigos if codigos.count(c) > 1})

    if duplicados:
        print("ATENÇÃO: foram encontrados códigos duplicados:")
        for codigo in duplicados[:20]:
            print("  ", codigo)
        raise RuntimeError(
            f"Foram encontrados {len(duplicados)} códigos duplicados."
        )

    with CSV_PATH.open("w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(
            f,
            fieldnames=[
                "codigo",
                "codigo_distrito",
                "codigo_concelho",
                "codigo_freguesia",
                "concelho",
                "nome",
                "extinta",
            ],
        )
        writer.writeheader()
        writer.writerows(registos)

    ativos = sum(1 for r in registos if r["extinta"] == "false")
    extintos = sum(1 for r in registos if r["extinta"] == "true")

    print()
    print("CONVERSÃO CONCLUÍDA")
    print(f"PDF: {PDF_PATH}")
    print(f"CSV: {CSV_PATH}")
    print(f"Registos: {len(registos)}")
    print(f"Ativos: {ativos}")
    print(f"Extintos: {extintos}")
    print(f"Linhas ignoradas: {len(ignoradas)}")

    if ignoradas:
        print()
        print("Primeiras linhas ignoradas:")
        for page_num, linha in ignoradas[:30]:
            print(f"Página {page_num}: {linha}")


if __name__ == "__main__":
    main()