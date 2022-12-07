package steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.Assert;

import java.time.LocalTime;

public class Steps {
    String uri = "http://localhost:8080";
    ValidatableResponse resp;
    int startWorkingHour = 8;
    int endWorkingHour = 22;
    String bodyGetResponseInfoRooms;
    int infoUsersStatusCode;
    String infoUsersBody;

    @Before
    public void before() {
        int hour = LocalTime.now().getHour();
        Assert.assertTrue("время не рабочее", (hour >= startWorkingHour && hour < endWorkingHour));
    }
    @When("^выполнить запрос для метода check когда пользователь с id (.*) пытается (войти|выйти) в комнату с id (.*)$")
    public void getCheckTest(int keyId, String entrance, int roomId) {
        String entrance2;
        if (entrance.equals("войти")) {
            entrance2 = "ENTRANCE";
        } else entrance2 = "EXIT";

        var response = RestAssured.given()
                .when().log().uri()
                .baseUri(uri)
                .param("entrance", entrance2)
                .param("keyId", keyId)
                .param("roomId", roomId)
                .request("GET", "/check")
                .then();
        resp = response;
        System.out.println(response.extract().statusCode());
        System.out.println(response.extract().response().body().asString());
    }
    @When("^выполнить запрос для метода check если пользователь с id (.*) пытается (.*) в комнату с id (.*)$")
    public void getCheckOutline(int keyId, String entrance, int roomId) {
        String entrance2 = null;
        if (entrance.equals("войти")) {
            entrance2 = "ENTRANCE";
        } else if ((entrance.equals("выйти"))) {
            entrance2 = "EXIT";
        } else System.out.println("не распознано вход или выход");

        var response = RestAssured.given()
                .when().log().uri()
                .baseUri(uri)
                .param("entrance", entrance2)
                .param("keyId", keyId)
                .param("roomId", roomId)
                .request("GET", "/check")
                .then();
        resp = response;
        System.out.println(response.extract().statusCode());
        System.out.println(response.extract().response().body().asString());
    }

    @Then("^проверить что код ответа метода check (.*)$")
    public void checkStatusCode(int expectedStatusCode) {
        int statusCode = resp.extract().statusCode();
        Assert.assertEquals("не совпал код ответа с ожидаемым ", expectedStatusCode, statusCode);
    }

    @And("^проверить, что тело ответа '(.*)'$")
    public void checkBody(String bodyExpected) {
        String bodyActual = resp.extract().response().body().asString();
        Assert.assertEquals("не совпало тело ответа с ожидаемым ", bodyExpected, bodyActual);
    }

    @When("^перебрать всех пользователей и все комнаты чтобы выяснить кто куда может войти$")
    public void checkUsersAndRooms() {
        for (int keyId = 1; keyId < 11; keyId++) {
            for (int roomId = 1; roomId < 6; roomId++) {
                if (keyId % roomId == 0) {
                    Assert.assertEquals("не совпал статус код", 200,
                            getCheck(roomId, keyId, "ENTRANCE").extract().statusCode());
                    getCheck(roomId, keyId, "EXIT");
                } else {
                    Assert.assertEquals("не совпал статус код", 403,
                            getCheck(roomId, keyId, "ENTRANCE").extract().statusCode());
                }
            }
        }
    }

    public ValidatableResponse getCheck(int roomId, int keyId, String entrance) {
        var response = RestAssured.given()
                .when().log().uri()
                .baseUri(uri)
                .param("entrance", entrance)
                .param("keyId", keyId)
                .param("roomId", roomId)
                .request("GET", "/check")
                .then();
        response.log().status();
        return response;
    }
    @When("^выполнить запрос infoRooms$")
    public void getResponseInfoRooms() {
        String response = RestAssured.given()
                .when().log().uri()
                .baseUri(uri)
                .request("GET", "/info/rooms")
                .then()
                .extract().body().asString();
        System.out.println(response);
        bodyGetResponseInfoRooms = response;
    }

    @When("^очистить все комнаты от пользователей$")
    public void clearAllRooms() {
        for (int userId = 1; userId < 11; userId++) {
            for (int roomId = 1; roomId < 6; roomId++) {
                getCheck(roomId, userId, "EXIT");
            }
        }
    }

    @Then("^проверить что пользователь с id (.*) отображается в комнате с id (.*)$")
    public void checkUserDisplayedRoom(int keyId, int roomId) {
        String bodyActual = "[{\"roomId\":" + keyId + ",\"userIds\":[" + roomId + "]}]";
        Assert.assertEquals("пользователь не входил в комнату ", bodyActual, bodyGetResponseInfoRooms);
    }

    @When("^запустить всех пользователей в первую комнату$")
    public void launchAllUsersFirstRoom() {
        int roomId = 1;
        for (int keyId = 1; keyId < 11; keyId++) {
            getCheck(roomId, keyId, "ENTRANCE");
        }
    }

    @Then("^проверить что в первой комнате все пользователи$")
    public void checkAllUsersFirstRoom() {
        String bodyActual = "[{\"roomId\":1,\"userIds\":[1,2,3,4,5,6,7,8,9,10]}]";
        Assert.assertEquals(" ", bodyActual, bodyGetResponseInfoRooms);
    }

    @Then("^проверить что id пустых комнат не отображаются$")
    public void checkRoomNumbersEmptyNotDisplayed() {
        String bodyActual = "[]";
        Assert.assertEquals("id пустых комнат отображаются", bodyActual, bodyGetResponseInfoRooms);
    }

    @When("^в каждую комнату запустить по одному пользователю у которого keyId равен roomId комнаты$")
    public void runIfKeyIdEqualRoomId () {
        for (int i = 1; i < 6; i++) {
            getCheck(i, i, "ENTRANCE");
        }
    }

    @Then("^проверить что в каждой комнате есть пользователь с keyId равным roomId комнаты$")
    public void checkEveryRoomUsersKeyIdEqualRoomId() {
        String actual =
                "[{\"roomId\":1,\"userIds\":[1]},{\"roomId\":2,\"userIds\":[2]},{\"roomId\":3,\"userIds\":[3]},{\"roomId\":4,\"userIds\":[4]},{\"roomId\":5,\"userIds\":[5]}]";
        Assert.assertEquals("id пользователя и id комнаты не совпадают", actual, bodyGetResponseInfoRooms);
    }

    @When("^отправить запрос infoUsers для параметров start= (\\d+) и end= (\\d+)$")
    public void sendRequestInfoUsersParameterStartИEnd(int start, int end) {
        ValidatableResponse response = RestAssured.given()
                .when().log().uri()
                .baseUri(uri)
                .param("start", start)
                .param("end", end)
                .request("GET", "/info/users")
                .then();
        int statCode = response.extract().statusCode();
        String body = response.extract().response().body().asString();
        System.out.println("infoUsers вернул код ответа " + statCode);
        System.out.println(body);
        infoUsersStatusCode = statCode;
        infoUsersBody = body;
    }

    @Then("^проверить что отображается информация обо всех пользователях$")
    public void checkInfoAllUsers() {
        String actualUsers =
                "[{\"id\":1,\"roomId\":1},{\"id\":2,\"roomId\":2},{\"id\":3,\"roomId\":3},{\"id\":4,\"roomId\":4},{\"id\":5,\"roomId\":5},{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9},{\"id\":10}]";
        Assert.assertEquals("отобразилась информация не обо всех пользователях", actualUsers, infoUsersBody);
    }

    @Then("^проверить что код ответа метода infoUsers = (.*)$")
    public void checkStatusCodeInfoUsers(int responseCode) {
        Assert.assertEquals("код ответа не совпал с ожидаемым", responseCode, infoUsersStatusCode);
    }

    @Then("^проверить, что все пользователи отображаются без комнат$")
    public void checkAllUsersDisplayed() {
        String actualUsers =
                "[{\"id\":1},{\"id\":2},{\"id\":3},{\"id\":4},{\"id\":5},{\"id\":6},{\"id\":7},{\"id\":8},{\"id\":9},{\"id\":10}]";
        Assert.assertEquals("пользователи без комнат не отобразились", actualUsers, infoUsersBody);
    }
}


