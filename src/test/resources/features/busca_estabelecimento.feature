# language: pt
@qualidade
Funcionalidade: Busca e filtragem de estabelecimentos

  Cenário: Cliente busca estabelecimentos por serviço e faixa de preço
    Dado que existem estabelecimentos oferecendo "Manicure" com preços entre 30 e 80
    Quando o cliente busca por "Manicure" com preço máximo de 50
    Então somente os estabelecimentos dentro da faixa de preço devem ser retornados
