# 🎮 Steam Alert 🎮

<img src="./img/banner.png" alt="Banner Steam Alert">

> Um bot que usa o Telegram para enviar notificações sobre jogos em promoção na plataforma Steam.

Steam Alert é um projeto que integra o Telegram e a Steam para te informar sobre as promoções de
jogos. Com ele, você pode se inscrever no bot, adicionar as contas da Steam que você quer seguir, e
receber mensagens com os jogos que estão na sua lista de desejos com preços reduzidos. O projeto foi
feito com o Java 17 e Spring Boot 3, além de um banco de dados MySQL e a biblioteca TelegramBots.

## 🚀 Getting started 🚀

Para usar o bot, você precisa ter uma conta no Telegram e seguir os seguintes passos:

1. Pesquise pelo bot [@SteamAlertBot](https://t.me/AlertSteamBot) no Telegram e inicie uma
   conversa com ele.
2. Digite ``/menu`` para acessar as opções do bot.
3. Selecione a opção ``Monitored accounts 🕵️`` para ver as contas da Steam que estão sendo
   monitoradas
   pelo bot. Se você não tiver nenhuma conta, selecione a opção ``Add ➕`` para adicionar uma.
4. Digite o ID da conta da Steam que você quer adicionar ao monitoramento do bot. Você pode
   encontrar o ID da sua conta usando sites
   como [SteamID Finder](https://www.steamidfinder.com/).
5. Aguarde a confirmação do bot de que a conta foi adicionada com sucesso.
6. Repita os passos 3 a 5 para adicionar mais contas, se desejar.
7. Aguarde as notificações do bot sobre os jogos em promoção na lista de desejos de cada conta. O
   bot verifica as promoções diáriamente e envia uma mensagem com os detalhes dos jogos, como
   nome, preço original, preço com desconto e link para a loja da Steam.
8. Se você quiser remover uma conta do monitoramento do bot, selecione a opção ``Monitored accounts
   🕵️`` e depois selecione a conta que você quer remover. Em seguida, selecione a
   opção ``Delete 🗑️`` e
   confirme a remoção.
9. Se você quiser cancelar a inscrição no bot, selecione a opção ``Unsubscribe 🚫`` e confirme o
   cancelamento.

## 🐳 Como rodar com Docker 🐳

Se você preferir rodar o projeto com Docker em sua máquina, você pode usar o docker compose para
subir rapidamente uma nova instância do bot. Você também precisa ter as credenciais da API da Steam
e do Telegram Bot, como explicado a seguir.

### Obter as credenciais da API da Steam

- Para obter a chave da API da Steam, você precisa ter uma conta na Steam e acessar o
  site [Steam Web API Key](https://steamcommunity.com/dev/apikey). Lá, você deve informar um
  domínio (
  pode ser qualquer um) e clicar em "Register". Você receberá uma chave alfanumérica que deve ser
  guardada em um lugar seguro.

### Obter as credenciais do Telegram Bot

- Para obter o token do Telegram Bot, você precisa ter uma conta no Telegram e conversar com
  o [@BotFather](https://t.me/botfather). Lá, você deve digitar `/newbot` e seguir as instruções
  para
  criar um novo bot. Você receberá um token alfanumérico que deve ser guardado em um lugar seguro.

Depois de obter as credenciais da API da Steam e do Telegram Bot, você deve editar o arquivo
docker-compose.yml, adicionando suas informações nas variáveis de ambiente:

#### docker-compose.yml:

```yaml
# Modifique as variáveis comentadas abaixo

version: '1'
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    depends_on:
      - db
    environment:
      - DB_URL=jdbc:mysql://db:3306/steam-alert-db?createDatabaseIfNotExist=true
      - DB_USER=root
      - DB_PASSWORD= #YOUR DB PASS
      - BOT_TOKEN= # YOUR BOT TOKEN
      - BOT_CREATOR_ID= # YOUR TELEGRAM ID
      - STEAM_API_KEY= # YOUR STEAM API KEY

  db:
    image: mysql
    environment:
      - MYSQL_DATABASE=steam-alert-db
      - MYSQL_ROOT_PASSWORD= #YOUR DB PASS
```

### Rodar o projeto com Docker Compose

Para rodar o projeto com Docker Compose, abra um terminal na pasta raiz do projeto e digite o
seguinte comando:

> docker-compose up

Isso irá construir as imagens dos serviços, criar os containers e iniciar a aplicação.

Para testar o bot, abra o Telegram e inicie uma conversa com o seu bot criado pelo BotFather. Você
deve ver as opções do menu e poder interagir com o bot.

Para parar o projeto com Docker Compose, abra um terminal na pasta raiz do projeto e digite o
seguinte comando:

> docker-compose down

Isso irá parar os containers e remover as imagens, redes e volumes criados pelo Docker Compose.

## 🤝 Reconhecimentos 🤝

- [Ruben Bermudez](https://github.com/rubenlagus): Por disponibilizar a
  biblioteca [TelegramBots](https://github.com/rubenlagus/TelegramBots)
  gratuitamente, facilitando a integração com a API do Telegram :D

## 📝 Licença 📝

Este projeto está licenciado sob a licença Apache - veja o arquivo [LICENSE](./LICENSE) para mais
detalhes.