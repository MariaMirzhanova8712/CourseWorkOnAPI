#language=en

@all
Feature: Проверка возможности входа в комнату (или выхода из комнаты) по ключу

#  позитивные проверки

  @checkingUsersAndRooms
  Scenario: Пользователю можно входить в комнаты,если номер комнаты (roomId) делится без остатка его ключ (keyId)
    When перебрать всех пользователей и все комнаты чтобы выяснить кто куда может войти

  @getKeyIdRoomId
  Scenario: проверить может ли пользователь с id войти в комнату с id
    When выполнить запрос для метода check когда пользователь с id 1 пытается войти в комнату с id 1
    Then проверить что код ответа метода check 200
    And проверить, что тело ответа 'You are welcome!'
    When выполнить запрос для метода check если пользователь с id 1 пытается выйти в комнату с id 1
    Then проверить что код ответа метода check 200
    And проверить, что тело ответа 'Goodbye!'

#  негативные проверки
  @outline
  Scenario Outline:
    When выполнить запрос для метода check если пользователь с id <keyId> пытается <entrance> в комнату с id <roomId>
    Then проверить что код ответа метода check <respCode>
    And проверить, что тело ответа '<body>'

    Examples:
      | keyId | entrance | roomId | respCode | body                                                |
#      попытаться войти куда нельзя
      | 1     | войти    | 10     | 403      | You has no privileges to enter this room            |
      | 3     | войти    | 5      | 403      | You has no privileges to enter this room            |
#  некорректные id
      | 1     | войти    | - 1    | 500      | There are no such room with id #-1 in repository    |
      | 3     | войти    | 352    | 500      | There are no such room with id #352 in repository   |
      | 4     | войти    | 0      | 500      | There are no such room with id #0 in repository     |
      | 20    | войти    | 1      | 500      | There are no such user with key #20 in repository   |
      | -73   | войти    | 1      | 500      | There are no such user with key #-73 in repository  |
      | 0     | войти    | 1      | 500      | There are no such user with key #0 in repository    |
#6. пользователь не может войти в одну комнату дважды
      | 5     | войти    | 5      | 200      | You are welcome!                                    |
      | 5     | войти    | 5      | 500      | You can't enter into room #5 without living room #5 |
#7. если пользователь находится в одной комнате то он не может войти в другую комнату
      | 2     | войти    | 2      | 200      | You are welcome!                                    |
      | 2     | войти    | 4      | 403      | There are no such room with id #2 in repository     |
#8. пользователь не может выйти из комнаты в которую он не зашел
      | 4     | войти    | 2      | 200      | You are welcome!                                    |
      | 4     | выйти    | 4      | 500      | You can't enter into room #4 without living room #2 |
