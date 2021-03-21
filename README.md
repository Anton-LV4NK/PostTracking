Данный проект реализует REST API сервис, который позволяет отслеживать почтовые отправления.

В проекте использавался стек технологий - org.apache.logging, org.yaml,  org.json, org.apache.httpcomponents, org.hibernate, org.postgresql, junit, maven
Для успешного старта проекта на машине необходимо иметь установленный Apache Maven фреймворк (не ниже 3.6.0), СУБД org.postgresql (не ниже 10.16) на стандартном порту 5432.
Создать базу данных в СУБД postgresql с именем posttracking.
Все настройки проекта находятся в файле /путь к проекту/PostTracking/src/main/java/com/PostTracking/settings.yaml.
После сборки Maven (mvn clean install -U) запустить командой c параметрами из файла настроек (/путь к проекту/PostTracking/src/main/java/com/PostTracking/settings.yaml.).
java -jar target/PostTracking-1.0-SNAPSHOT.jar /путь к проекту/PostTracking/src/main/java/com/PostTracking/settings.yaml

В системе реализован функционал согласно ТЗ:
- регистрация почтового отправления (метод запроса протокола http - POST):
  эндпоинт 	/post/register_post/
  тело метода	"{'index':...,'name':'...','address':'...'}"

  index - индекс почтового отделения.
  name - название почтового отделения.
  address - индекс почтового отделения.

  возвращается в ответе занесенный в базу объект, т.е. "{'index':...,'name':'...','address':'...'}", или "{}" - если объект уже существует

- его прибытие в промежуточное почтовое отделение (метод запроса протокола http - POST):
  эндпоинт 	/items/arrival/
  тело метода	"{'type':...,'indexSender':...,'indexReceiver':..., 'addressRecipient':'...', 'nameRecipient':'...', 'identifier':..., 'postIndex':...}"

  type - тип почтового отправления (посылка, письмо).
  indexSender - индекс отправителя.
  indexReceiver - индекс получателя.
  addressRecipient - адресс получателя.
  nameRecipient - имя получателя.
  identifier - уникальный идентификатор почтового отправления.
  postIndex - индекс почтового отделения, куда прибыло почтовое отправление.

  возвращается в ответе занесенный в базу объект с полученным id и кодом состояния почтового отправления (statusCode), {"identifier":...,"nameRecipient":"...","indexSender":...,"id":...,"type":...,"indexReceiver":...,"addressRecipient":"..., Rostov street","statusCode":...}

-  его убытие из почтового отделения (метод запроса протокола http - POST):
   эндпоинт 	/items/departure/
   тело метода	"{'identifier':..., 'postIndex':...}"

   identifier - уникальный идентификатор почтового отправления.
   postIndex - индекс почтового отделения, куда убыло почтовое отправление.

   возвращается в ответе {"identifier":...,"nameRecipient":"...","indexSender":...,"id":...,"type":...,"indexReceiver":...,"addressRecipient":"...","statusCode":...}

- его получение адресатом (метод запроса протокола http - POST):
  эндпоинт 	/items/received/
  тело метода     "{'identifier':..., 'postIndex':...}"

  identifier - уникальный идентификатор почтового отправления.
  postIndex - индекс почтового отделения, куда убыло почтовое отправление.

  возвращается в ответе  {"identifier":...,"nameRecipient":"...","indexSender":...,"id":...,"type":...,"indexReceiver":...,"addressRecipient":"...","statusCode":...}

- просмотр статуса и полной истории движения почтового отправления (метод запроса протокола http - POST):
  эндпоинт 	/items/history/
  тело метода     "{'identifier':...}"

  identifier - уникальный идентификатор почтового отправления.

  возвращается в ответе JSON массив объектов по ключу history определяющих перемещение почтового отправления и его статус на данный момент,
  {"history":[
  {"statusItem":...,"transferDate":...,"idItem":...,"idPost":12345},...
  ]}
  согласно коду состояния почтового отправления statusItem определяется статус на данный момент, transferDate представляет собой время в формате UNIX, в которое совершено действие над почтовым отправлением.
  Актуальный код состояния почтового отправления (statusItem), соответственно, у элемента массива JSON с самым большим transferDate (время последнего перемещения почтового отправления).

Коды состояния почтового отправления (поле statusCode):
1 - регистрация нового почтового отправления на почте.
2 - прибытие почтового отправления на почту.
3 - убытие почтового отправления из почты.
4 - получение почтового отправления клиентом.