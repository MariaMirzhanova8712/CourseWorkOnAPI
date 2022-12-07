#language=en

@all
Feature: Получить информацию по всем пользователям (ключам)

@allUsers
  Scenario: вывести список всех пользователей
    When очистить все комнаты от пользователей
    When в каждую комнату запустить по одному пользователю у которого keyId равен roomId комнаты
    When отправить запрос infoUsers для параметров start= 0 и end= 20
    Then проверить что отображается информация обо всех пользователях

  @allUsersNoRooms
  Scenario: проверить, что при start=0 end=20 и пустых комнатах метод infoUsers покажет всех пользователей без комнат
    When очистить все комнаты от пользователей
    When отправить запрос infoUsers для параметров start= 0 и end= 20
    Then проверить что код ответа метода infoUsers = 200
    Then проверить, что все пользователи отображаются без комнат

    @requestInfoUsers
  Scenario: отправить запрос infoUsers
    When отправить запрос infoUsers для параметров start= 0 и end= 20
    Then проверить что код ответа метода infoUsers = 200

  @infoUsersInputTest
  Scenario Outline:
    When отправить запрос infoUsers для параметров start= <start> и end= <end>
    Then проверить что код ответа метода infoUsers = <responseCode>

    Examples:
      | start | end | responseCode |
      | 0     | 20  | 200          |
      | -1    | 20  | 500          |
#  случай когда end < start
      | 10    | 2   | 500          |
