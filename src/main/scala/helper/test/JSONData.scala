package helper.test

/**
  * Created by Sascha on 31.05.2017.
  */
object JSONData {

  object ActionChain {
    val withoutMapping = "{\n  \"type\" : \"HttpRequestAction\",\n  \"name\" : \"Post Parsed Action\",\n  \"method\" : \"POST\",\n  \"url\" : \"http://localhost:3000/person\",\n  \"body\" : \"{\\\"name\\\":\\\"Max\\\",\\\"knows\\\":[],\\\"items\\\":[]}\",\n  \"mappings\" : [ ],\n  \"params\" : [ {\n    \"attribute\" : \"TEST3\",\n    \"value\" : \"TEST3\"\n  }, {\n    \"attribute\" : \"TEST4\",\n    \"value\" : \"TEST4\"\n  } ],\n  \"headers\" : [ {\n    \"attribute\" : \"TEST1\",\n    \"value\" : \"TEST1\"\n  }, {\n    \"attribute\" : \"TEST2\",\n    \"value\" : \"TEST2\"\n  } ],\n  \"next\" : [ {\n    \"type\" : \"PrintResponseAction\",\n    \"name\" : \"Print Parsed Action\",\n    \"next\" : [ {\n      \"type\" : \"HttpRequestAction\",\n      \"name\" : \"Get Parsed Action\",\n      \"method\" : \"GET\",\n      \"url\" : \"http://localhost:3000/persons\",\n      \"body\" : \"\",\n      \"mappings\" : [ ],\n      \"params\" : [ ],\n      \"headers\" : [ ],\n      \"next\" : [ {\n        \"type\" : \"PrintResponseAction\",\n        \"name\" : \"Print Parsed Action\",\n        \"next\" : [ ]\n      } ]\n    } ]\n  } ]\n}"
    val emptyArrays = "{\n      \"type\" : \"HttpRequestAction\",\n      \"name\" : \"Post Parsed Action\",\n      \"method\" : \"POST\",\n      \"url\" : \"http://localhost:3000/person\",\n      \"body\" : \"{\\\"name\\\":\\\"Max\\\",\\\"knows\\\":[],\\\"items\\\":[]}\",\n      \"mappings\" : [ ],\n      \"params\" : [ ],\n      \"headers\" : [ ],\n      \"next\" : [ {\n      \"type\" : \"PrintResponseAction\",\n      \"name\" : \"Print Parsed Action\",\n      \"next\" : [ {\n        \"type\" : \"HttpRequestAction\",\n        \"name\" : \"Get Parsed Action\",\n        \"method\" : \"GET\",\n        \"url\" : \"http://localhost:3000/persons\",\n        \"body\" : \"\",\n        \"mappings\" : [ ],\n        \"params\" : [ ],\n        \"headers\" : [ ],\n        \"next\" : [ {\n          \"type\" : \"PrintResponseAction\",\n          \"name\" : \"Print Parsed Action\",\n          \"next\" : [ ]\n        } ]\n      } ]\n    } ]\n    }"
  }

  object Action {
    val withoutMapping = "{\n  \"type\" : \"HttpRequestAction\",\n  \"name\" : \"Post Parsed Action\",\n  \"method\" : \"POST\",\n  \"url\" : \"http://localhost:3000/person\",\n  \"body\" : \"{\\\"name\\\":\\\"Max\\\",\\\"knows\\\":[],\\\"items\\\":[]}\",\n  \"mappings\" : [ ],\n  \"params\" : [ {\n    \"attribute\" : \"TEST3\",\n    \"value\" : \"TEST3\"\n  }, {\n    \"attribute\" : \"TEST4\",\n    \"value\" : \"TEST4\"\n  } ],\n  \"headers\" : [ {\n    \"attribute\" : \"TEST1\",\n    \"value\" : \"TEST1\"\n  }, {\n    \"attribute\" : \"TEST2\",\n    \"value\" : \"TEST2\"\n  } ],\n  \"next\" : [ ]\n}"
    val emptyArrays = "{\n      \"type\" : \"HttpRequestAction\",\n      \"name\" : \"Post Parsed Action\",\n      \"method\" : \"POST\",\n      \"url\" : \"http://localhost:3000/person\",\n      \"body\" : \"{\\\"name\\\":\\\"Max\\\",\\\"knows\\\":[],\\\"items\\\":[]}\",\n      \"mappings\" : [ ],\n      \"params\" : [ ],\n      \"headers\" : [ ],\n      \"next\" : [ ]\n    }"
    val missingArrays = "{\n      \"type\" : \"HttpRequestAction\",\n      \"name\" : \"Post Parsed Action\",\n      \"method\" : \"POST\",\n      \"url\" : \"http://localhost:3000/person\",\n      \"body\" : \"{\\\"name\\\":\\\"Max\\\",\\\"knows\\\":[],\\\"items\\\":[]}\" }"
  }

}
