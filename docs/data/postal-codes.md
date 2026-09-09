# Códigos postais portugueses

O catálogo previsto para o FAC utiliza o ficheiro `PT.txt` dos GeoNames Postal Code files (dataset PT). A licença declarada no `readme.txt` do pacote é **Creative Commons Attribution 4.0 (CC BY 4.0)**.

O ficheiro original (`PT.zip`) ainda não é distribuído no repositório. Quando a importação for implementada, a documentação deve manter a atribuição à fonte GeoNames e apontar para <https://www.geonames.org/> e para os termos da licença <https://creativecommons.org/licenses/by/4.0/>.

O modelo separa o código postal (`codpostal`) das localidades alternativas (`codpostal_localidade`). A carga futura deve validar códigos `NNNN-NNN`, deduplicar por código, preservar todas as associações e produzir um relatório idempotente.
