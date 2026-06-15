## Revisão controlada do CRUD de séries documentais

Analisa e ajusta o CRUD existente da entidade `Serie`, preservando a arquitetura atual do projeto FAC e evitando refatorações desnecessárias.

A entidade atual é:

```java
package com.ar2lda.fac.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "serie")
@IdClass(SerieId.class)
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class Serie {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    @Setter
    @ToString.Include
    private TipoDocumento tipoDocumento;

    @Id
    @Column(length = 10, nullable = false)
    @ToString.Include
    private String serie;

    @Column(length = 50, nullable = false)
    @Setter
    @ToString.Include
    private String nome;

    @Column(name = "codigo_at", length = 100)
    @Setter
    private String codigoAt;

    @Column(name = "data_codigo_at")
    @Setter
    private LocalDate dataCodigoAt;

    @Column(nullable = false)
    @ToString.Include
    private Long numerador = 0L;

    protected Serie() {
    }

    public Serie(
            TipoDocumento tipoDocumento,
            String serie,
            String nome,
            String codigoAt,
            LocalDate dataCodigoAt
    ) {
        this.tipoDocumento = tipoDocumento;
        this.serie = serie;
        this.nome = nome;
        this.codigoAt = codigoAt;
        this.dataCodigoAt = dataCodigoAt;
    }

    public Long proximoNumero() {
        numerador++;
        return numerador;
    }

    public boolean temCodigoAt() {
        return codigoAt != null && !codigoAt.isBlank();
    }
}
```

O DTO de criação atual é:

```java
package com.ar2lda.fac.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SerieCreateDto(
        @NotBlank(message = "Série é obrigatória")
        @Size(max = 10, message = "Série deve ter no máximo 10 caracteres")
        String serie,

        @NotBlank(message = "Tipo de documento é obrigatório")
        @Size(min = 3, max = 3, message = "Tipo de documento deve ter 3 caracteres")
        String tipoDocumentoId,

        @NotBlank(message = "Nome da série é obrigatório")
        @Size(max = 50, message = "Nome da série deve ter no máximo 50 caracteres")
        String nome,

        @Size(max = 100, message = "Código AT deve ter no máximo 100 caracteres")
        String codigoAt,

        LocalDate dataCodigoAt
) {
}
```

---

# Objetivo

Rever o CRUD já existente de séries para garantir:

* coerência com a chave composta;
* imutabilidade da identidade da série;
* normalização dos dados textuais;
* proteção do numerador;
* controlo da alteração e eliminação de séries já utilizadas;
* preparação mínima para futura utilização do código de validação da AT;
* manutenção do comportamento atual sempre que este já estiver correto.

Esta tarefa não inclui ainda:

* geração do ATCUD;
* geração do Código QR;
* comunicação automática de séries à AT;
* criação de novos estados complexos para a série;
* criação de uma nova entidade `SerieDocumental`;
* implementação completa do processo de emissão documental;
* alterações profundas à arquitetura atual.

---

# 1. Inspeção inicial obrigatória

Antes de alterar código, analisa:

* `Serie`;
* `SerieId`;
* `TipoDocumento`;
* `SerieRepository`;
* DTOs existentes de série;
* mapper existente;
* service existente;
* controller existente;
* testes existentes;
* frontend ou formulários que consumam este CRUD;
* locais onde `proximoNumero()` é chamado;
* relações entre `Serie` e documentos comerciais ou financeiros.

Não cries classes duplicadas se já existirem equivalentes.

Mantém os nomes, padrões e convenções atuais do projeto sempre que sejam adequados.

---

# 2. Confirmar a chave de `TipoDocumento`

Confirma qual é o tipo real do identificador de `TipoDocumento`.

O DTO atual utiliza:

```java
String tipoDocumentoId
```

e exige exatamente três caracteres.

Não alteres esta validação sem confirmar a entidade `TipoDocumento` e os dados reais utilizados no projeto.

Se os identificadores existentes tiverem três caracteres, mantém:

```java
@Size(min = 3, max = 3)
```

Se o comprimento real for diferente, corrige a validação de forma coerente com a entidade e com os dados existentes.

Não assumes automaticamente que o identificador corresponde aos códigos SAF-T de dois caracteres.

---

# 3. Imutabilidade da identidade

A identidade da entidade é composta por:

```text
tipoDocumento + serie
```

Depois de criada, esta identidade não deve ser alterada.

Remove o `@Setter` de:

```java
private TipoDocumento tipoDocumento;
```

O campo:

```java
private String serie;
```

deve continuar sem setter.

Os campos que constituem a chave devem ser definidos apenas no construtor de criação.

O DTO e o endpoint de atualização não devem permitir alterar:

* `tipoDocumento`;
* `serie`;
* `numerador`.

---

# 4. Normalização de dados

Normaliza os valores textuais antes de procurar ou persistir:

* `serie`;
* `tipoDocumentoId`;
* `nome`;
* `codigoAt`.

Regras:

```text
serie:
trim obrigatório

tipoDocumentoId:
trim obrigatório
respeitar a convenção atual de maiúsculas/minúsculas

nome:
trim obrigatório

codigoAt:
null se nulo, vazio ou apenas espaços
caso contrário, trim
```

Evita que valores como:

```text
"2026"
"2026 "
```

sejam tratados como séries diferentes.

A normalização deve ficar centralizada no service ou em métodos apropriados, evitando duplicação.

---

# 5. DTO de criação

Mantém o `SerieCreateDto`, ajustando-o apenas se a inspeção do modelo demonstrar necessidade.

O DTO de criação deve continuar a permitir:

* série;
* tipo de documento;
* nome;
* código AT;
* data do código AT.

Não deve receber:

* numerador;
* próximo número;
* último número;
* identificadores técnicos desnecessários.

---

# 6. DTO de atualização

Confirma se já existe um DTO específico de atualização.

Se não existir, cria um DTO semelhante a:

```java
public record SerieUpdateDto(
        @NotBlank(message = "Nome da série é obrigatório")
        @Size(max = 50, message = "Nome da série deve ter no máximo 50 caracteres")
        String nome,

        @Size(max = 100, message = "Código AT deve ter no máximo 100 caracteres")
        String codigoAt,

        LocalDate dataCodigoAt
) {
}
```

O DTO de atualização não deve conter:

```text
tipoDocumentoId
serie
numerador
```

Não reutilizar o DTO de criação na atualização se isso permitir enviar ou tentar alterar a chave composta.

---

# 7. DTO de resposta

Confirma o DTO de resposta existente.

Deve expor, pelo menos:

```text
tipoDocumentoId
serie
nome
codigoAt
dataCodigoAt
numerador
```

Pode também expor:

```text
temCodigoAt
```

utilizando o método:

```java
serie.temCodigoAt()
```

Evita expor diretamente a entidade JPA no controller.

---

# 8. Criação da série

No service de criação:

1. normalizar os valores;
2. localizar `TipoDocumento`;
3. validar a existência do tipo de documento;
4. construir a chave composta;
5. confirmar que a série ainda não existe;
6. validar os dados do código AT;
7. criar a entidade;
8. persistir;
9. devolver DTO.

Não permitir que o cliente defina o numerador.

Uma série nova deve iniciar com:

```java
numerador = 0L;
```

---

# 9. Coerência entre código AT e data

Implementa uma validação proporcional e simples.

Regra recomendada:

```text
Se codigoAt estiver preenchido, dataCodigoAt também deve estar preenchida.
Se dataCodigoAt estiver preenchida, codigoAt também deve estar preenchido.
```

Exemplo de mensagem:

```text
O código AT e a respetiva data devem ser preenchidos em conjunto.
```

Antes de aplicar esta regra, confirma se o comportamento atual do projeto ou dados já existentes depende de a data ser opcional.

Se existirem registos históricos com código AT e data nula, não cries uma incompatibilidade silenciosa. Nesse caso:

* documenta a situação;
* mantém compatibilidade;
* indica no relatório final a validação que ficou pendente.

---

# 10. Atualização da série

A atualização deve localizar a série através da chave existente:

```text
tipoDocumentoId + serie
```

Pode permitir alterar:

* nome;
* código AT;
* data do código AT.

Não pode permitir alterar:

* tipo de documento;
* código da série;
* numerador.

Adiciona a seguinte regra:

```text
Se numerador > 0, o código AT e a respetiva data não podem ser alterados.
```

O nome da série pode continuar editável, por ser uma descrição funcional e não fazer parte da identidade fiscal.

Mensagem sugerida:

```text
Não é possível alterar o código AT de uma série já utilizada.
```

Compara os valores normalizados para não considerar como alteração apenas a remoção de espaços.

---

# 11. Eliminação

Uma série só pode ser eliminada quando ainda não tiver sido utilizada.

Regra mínima:

```text
numerador == 0
```

Se existirem relações com documentos, confirma também que não existem documentos associados.

Se a série já tiver sido utilizada, rejeitar a eliminação com uma mensagem clara:

```text
Não é possível eliminar uma série já utilizada.
```

Não implementar ainda estados de suspensão ou finalização, salvo se esses estados já existirem no projeto.

---

# 12. Proteção do numerador

O `numerador`:

* não deve existir nos DTOs de criação ou atualização;
* não deve ser editável pelo frontend;
* não deve possuir setter público;
* não deve ser alterado pelo CRUD normal;
* só deve evoluir através de `proximoNumero()`.

Não criar um endpoint CRUD para incrementar o numerador.

Mantém:

```java
public Long proximoNumero() {
    numerador++;
    return numerador;
}
```

mas analisa todos os locais onde este método é chamado.

No relatório final, indica:

* onde é chamado;
* se a chamada ocorre dentro de `@Transactional`;
* se existe proteção contra emissões concorrentes;
* se existe `PESSIMISTIC_WRITE`, `@Version` ou outro mecanismo.

Não implementar já um novo mecanismo concorrencial sem primeiro avaliar o fluxo real de emissão.

Se existir risco evidente de dois documentos obterem o mesmo número, propõe ou implementa a correção mínima compatível com a arquitetura atual, acompanhada de teste.

---

# 13. Repository

Mantém `JpaRepository<Serie, SerieId>` ou a estrutura equivalente já existente.

Confirma se existem métodos redundantes.

Acrescenta apenas os métodos realmente necessários ao CRUD, por exemplo:

```java
List<Serie> findByTipoDocumentoIdOrderBySerie(String tipoDocumentoId);
```

A assinatura exata deve respeitar:

* o nome real da propriedade identificadora de `TipoDocumento`;
* o tipo real do ID;
* as convenções atuais do repository.

Não acrescentar uma coleção de métodos especulativos.

---

# 14. Controller

Mantém os endpoints existentes sempre que sejam coerentes e já estejam a ser utilizados pelo frontend.

Não alteres URLs sem necessidade.

Garante que:

* criação utiliza `SerieCreateDto`;
* atualização utiliza `SerieUpdateDto`;
* respostas utilizam DTO;
* eliminação aplica as regras do service;
* erros de entidade inexistente são tratados pelo mecanismo global já existente;
* validação Bean Validation é aplicada através de `@Valid`.

A chave composta deve ser recebida de forma clara, usando o padrão atual do projeto.

---

# 15. Mapper

Reutiliza o mapper existente.

Se o projeto usa MapStruct, mantém MapStruct.

Não introduzas um mapper manual paralelo sem necessidade.

O mapper não deve consultar repositories.

A resolução de `TipoDocumento` deve permanecer no service.

Garante que o mapper de atualização não altera:

* tipoDocumento;
* serie;
* numerador.

---

# 16. Testes

Cria ou atualiza testes para os seguintes casos.

## Criação

* criar série válida;
* normalizar espaços;
* rejeitar série duplicada para o mesmo tipo documental;
* permitir a mesma designação de série em tipos documentais distintos, se o modelo atual assim o permitir;
* rejeitar tipo de documento inexistente;
* validar comprimentos máximos;
* validar coerência entre código AT e data, caso essa regra seja aplicada.

## Atualização

* alterar nome;
* alterar código AT enquanto `numerador == 0`;
* impedir alteração do código AT quando `numerador > 0`;
* confirmar que tipo de documento não é alterável;
* confirmar que série não é alterável;
* confirmar que numerador não é alterável;
* normalizar valores.

## Eliminação

* eliminar série nunca utilizada;
* impedir eliminação quando `numerador > 0`;
* impedir eliminação quando existem documentos associados, se aplicável;
* devolver erro adequado quando a série não existe.

## Consultas

* obter pela chave composta;
* listar séries;
* filtrar por tipo documental, se já existir essa funcionalidade.

## Numeração

* confirmar que `proximoNumero()` incrementa corretamente;
* confirmar que o numerador inicial é zero;
* confirmar que o numerador não é alterado por criação ou atualização via DTO.

---

# 17. Compatibilidade

Antes de concluir:

* executar todos os testes do backend;
* verificar se o frontend continua a compilar;
* verificar se os formulários atuais continuam compatíveis;
* verificar se houve alteração de contratos REST;
* não alterar a base de dados sem necessidade;
* não criar migrações redundantes;
* não renomear colunas existentes apenas por preferência estética.

Não renomear agora:

```text
codigo_at
data_codigo_at
```

salvo se existir uma razão técnica objetiva e migração segura.

---

# 18. Limites da intervenção

Não implementar nesta tarefa:

* ATCUD;
* Código QR;
* payload fiscal QR;
* comunicação de séries à AT;
* ambiente de testes ou produção;
* máquina de estados da série;
* encerramento de séries;
* nova entidade para séries;
* alteração geral do processo de emissão;
* reestruturação extensa dos documentos comerciais.

Esta tarefa destina-se apenas a consolidar o CRUD atual e proteger a identidade, o código AT e o numerador.

---

# 19. Resultado esperado

No final, apresenta um relatório conciso com:

1. ficheiros analisados;
2. ficheiros alterados;
3. classes criadas;
4. regras de negócio implementadas;
5. contratos REST alterados, se existirem;
6. testes criados ou atualizados;
7. resultado dos testes;
8. situação atual da segurança concorrencial do numerador;
9. eventuais pontos que ficaram pendentes;
10. confirmação de que não foi implementado ATCUD, QR Code ou comunicação automática à AT.

Não avances para funcionalidades fiscais adicionais sem indicação expressa.
