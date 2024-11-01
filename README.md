# Обменник валют

REST API для описания валют и обменных курсов. Позволяет просматривать и редактировать списки валют и обменных курсов, и совершать расчёт конвертации произвольных сумм из одной валюты в другую.

## Технологии
- Jakarta EE
- JDBC
- Maven
- Docker
- SQLite

## Запросы 

#### Валюты

- GET `/currencies`

Получение списка валют. Пример ответа:

```json
[
    {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },   
    {
        "id": 0,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    }
]
```


- GET `/currency/EUR`

Получение конкретной валюты. Пример ответа:

```json
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```

- POST `/currencies`

Добавление новой валюты в базу. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Поля формы - `name`, `code`, `sign`. Пример ответа - JSON представление вставленной в базу записи, включая её ID:

```json
{
    "id": 0,
    "name": "Euro",
    "code": "EUR",
    "sign": "€"
}
```


#### Обменные курсы 

- GET `/exchangeRates`

Получение списка всех обменных курсов. Пример ответа:

```json
[
    {
        "id": 0,
        "baseCurrency": {
            "id": 0,
            "name": "United States dollar",
            "code": "USD",
            "sign": "$"
        },
        "targetCurrency": {
            "id": 1,
            "name": "Euro",
            "code": "EUR",
            "sign": "€"
        },
        "rate": 0.99
    }
]
```


- GET `/exchangeRate/USDRUB`

Получение конкретного обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Пример ответа:

```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```


- POST `/exchangeRates`

Добавление нового обменного курса в базу. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Поля формы - `baseCurrencyCode`, `targetCurrencyCode`, `rate`. Пример полей формы:

- `baseCurrencyCode` - USD
- `targetCurrencyCode` - EUR
- `rate` - 0.99
    
Пример ответа - JSON представление вставленной в базу записи, включая её ID:

```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```


- PATCH `/exchangeRate/USDRUB`

Обновление существующего в базе обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса. Данные передаются в теле запроса в виде полей формы (`x-www-form-urlencoded`). Единственное поле формы - `rate`.

Пример ответа - JSON представление обновлённой записи в базе данных, включая её ID:

```json
{
    "id": 0,
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Euro",
        "code": "EUR",
        "sign": "€"
    },
    "rate": 0.99
}
```


#### Обмен валюты

- GET `/exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`

Расчёт перевода определённого количества средств из одной валюты в другую. Пример запроса - GET /exchange?from=USD&to=AUD&amount=10.

Пример ответа:

```json
{
    "baseCurrency": {
        "id": 0,
        "name": "United States dollar",
        "code": "USD",
        "sign": "$"
    },
    "targetCurrency": {
        "id": 1,
        "name": "Australian dollar",
        "code": "AUD",
        "sign": "A€"
    },
    "rate": 1.45,
    "amount": 10.00,
    "convertedAmount": 14.50
}
```


Получение курса для обмена может пройти по одному из трёх сценариев. Допустим, совершаем перевод из валюты **A** в валюту **B**:

  -  В таблице ExchangeRates существует валютная пара **AB** - берём её курс;
  -  В таблице ExchangeRates существует валютная пара **BA** - берем её курс, и считаем обратный, чтобы получить **AB**;
  -  В таблице ExchangeRates существуют валютные пары **USD-A** и **USD-B** - вычисляем из этих курсов курс **AB**.

Остальные возможные сценарии я опустил.


## Запуск приложения

Запускается приложение с помощью Docker. Перед использованием дальнейших команд убедитесь, что он у вас есть и обновлен.

- Для начала необходимо скопировать этот репозиторий:

```bash
git clone https://github.com/Faust32/currency-exchange.git
```

- После этого перейти в директорию с репозиторием и запустить команду:

```bash
docker build -t currency-exchange .
```

- И осталось лишь запустить контейнер:

```bash
docker run -d -p 8080:8080 currency-exchange
```

- После чего приложение будет доступно по адресу `http://localhost:8080/api/`.
