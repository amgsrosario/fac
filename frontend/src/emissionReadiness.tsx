export type EmissionAssessment = {
  confirmacaoRecalculoNecessaria: boolean;
  totalApresentado: number | string | null;
  totalProjectado: number | string | null;
  impedimentos: { codigo: string; mensagem: string }[];
  avisos: { codigo: string; mensagem: string }[];
};
export type EmissionDiagnostic = {
  podeEmitir: boolean;
  avaliacaoEmissao?: EmissionAssessment;
};
export function emissionConfirmation(diagnostic: EmissionDiagnostic, reference: string): string {
  const assessment = diagnostic.avaliacaoEmissao;
  const messages = assessment?.avisos.map(item => item.mensagem) ?? [];
  if (assessment && assessment.totalProjectado != null
      && Number(assessment.totalApresentado) !== Number(assessment.totalProjectado)) {
    messages.push(`Total apresentado: ${assessment.totalApresentado}. Total projectado: ${assessment.totalProjectado}.`);
  }
  return [...messages, `Emitir definitivamente ${reference}? Confirma os valores apresentados.`].join("\n");
}
export function EmissionReadinessMessages({ diagnostic }: { diagnostic: EmissionDiagnostic | null }) {
  const assessment = diagnostic?.avaliacaoEmissao;
  if (!assessment) return null;
  return <div aria-label="Condições de emissão">
    {assessment.impedimentos.map(item => <p role="alert" key={item.codigo}>{item.mensagem}</p>)}
    {assessment.avisos.map(item => <p key={item.codigo}>{item.mensagem}</p>)}
    {assessment.totalProjectado != null && Number(assessment.totalApresentado) !== Number(assessment.totalProjectado)
      && <p>Total apresentado: {assessment.totalApresentado}. Total projectado: {assessment.totalProjectado}.</p>}
  </div>;
}
