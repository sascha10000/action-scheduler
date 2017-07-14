import java.util.Locale

import io.vertx.core.json.{JsonArray, JsonObject}
import net.redhogs.cronparser.CronExpressionDescriptor
import org.scalatest.{FlatSpec, Matchers}
import scheduler.workflows.mappers.HttpResponseMapper

/**
  * Created by Sascha on 04.07.2017.
  */
class BasicTestSpec extends FlatSpec with Matchers {
  "Regex" should " validate" in {
    val someTest = "$#date(yyyy-MM-dd)#$T$#date(HH:mm:ss.SSS)#$Z"
    println(HttpResponseMapper.replaceDateFormatToday(someTest))

    // TODO: Löschen. Hat ursprünglich aus ein paar fixen daten Scheduler Einträge erzeugt
    val src = "[\n    {\n        \"checkliste\": \"Lohnbuchhaltung\",\n        \"client\": \"ECONS Gmbh\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e97e75edcfa5ce4d7a4c6\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"ECONS Gmbh\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e97cb5edcfa5ce4d7a4c5\"\n    },\n    {\n        \"checkliste\": \"Lohnbuchhaltung\",\n        \"client\": \"Udo Meyer Vermietung\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"591c31185edcfa5ce4d7a4c3\"\n    },\n    {\n        \"checkliste\": \"Quartalscheckliste\",\n        \"client\": \"Udo Meyer Vermietung\",\n        \"schedule\": \"0 1 0 1 1/3 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"58e3a4ec5edcfa38a024a80a\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Udo Meyer\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e983d5edcfa5ce4d7a4c8\"\n    },\n    {\n        \"checkliste\": \"Quartalscheckliste\",\n        \"client\": \"Udo Meyer\",\n        \"schedule\": \"0 1 0 1 1/3 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"57e3aee2da605e3dd44e0c98\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Konso Frotz\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e984d5edcfa5ce4d7a4c9\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Imperative Art\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98625edcfa5ce4d7a4ca\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Frotz Sinzig\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98705edcfa5ce4d7a4cb\"\n    },\n    {\n        \"checkliste\": \"Lohnbuchhaltungen\",\n        \"client\": \"Frotz Sinzig\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"59300b2d5edcfa5ce4d7a4d7\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Imperative Medicine\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98915edcfa5ce4d7a4cc\"\n    },\n    {\n        \"checkliste\": \"Lohnbuchhaltungen\",\n        \"client\": \"Imperative Medicine\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98925edcfa5ce4d7a4cd\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"iNUX UG\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98aa5edcfa5ce4d7a4ce\"\n    },\n    {\n        \"checkliste\": \"Quartalscheckliste\",\n        \"client\": \"Stiftung Deutsches Adelsarchiv\",\n        \"schedule\": \"0 1 0 1 1/3 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98c25edcfa5ce4d7a4d0\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Stiftung Deutsches Adelsarchiv\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e98bf5edcfa5ce4d7a4cf\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Wieco\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"Fibu\",\n        \"id\": \"592e99085edcfa5ce4d7a4d1\"\n    },\n    {\n        \"checkliste\": \"Lohnbuchhaltungen\",\n        \"client\": \"Wieco\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e990b5edcfa5ce4d7a4d2\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Zahnarztpraxis T. Meyer\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"Fibu\",\n        \"id\": \"592e992e5edcfa5ce4d7a4d3\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"Zahnarztpraxis T. Meyer\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"Lohnbuchhaltungen\",\n        \"id\": \"592e99305edcfa5ce4d7a4d4\"\n    },\n    {\n        \"checkliste\": \"Monatscheckliste\",\n        \"client\": \"iNUX\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e994f5edcfa5ce4d7a4d5\"\n    },\n    {\n        \"checkliste\": \"Lohnbuchhaltungen\",\n        \"client\": \"iNUX\",\n        \"schedule\": \"0 1 0 1 1/1 ? *\",\n        \"name_prefix\": \"\",\n        \"id\": \"592e99505edcfa5ce4d7a4d6\"\n    }\n]"
    val srcJson = new JsonArray(src)

    val target = "{\n  \"name\" : \"Copy Checklist <name checklist> at <scheduled time> for client <name client>\",\n  \"meta\": \"<meta>\",\n  \"schedule\": \"<schedule>\",\n  \"actions\":\n  [\n    {\n      \"type\" : \"HttpRequestAction\",\n      \"name\" : \"GET - Get Checklist\",\n      \"method\" : \"GET\",\n      \"url\" : \"http://192.168.166.1:8081/checklist/<id>\",\n      \"body\": \"\",\n      \"next\": [\n        {\n          \"type\" : \"HttpRequestAction\",\n          \"name\" : \"POST - POST Checklist\",\n          \"method\" : \"POST\",\n          \"url\" : \"http://192.168.166.1:8081/checklist\",\n          \"body\": \"\",\n          \"mappings\": [\n            {\n              \"from\": \"response.body\", \"to\": {\"field\": \"body\"},\n              \"toFunction\":\n              {\n                \"type\": \"JsonFieldReplacer\",\n                \"replace\": \"createdBy\",\n                \"with\": \"scheduler\",\n                \"function\":\n                {\n                  \"type\": \"JsonFieldReplacer\",\n                  \"replace\": \"createdAt\",\n                  \"with\": \"$#date()#$\",\n                  \"function\":\n                  {\n                    \"type\": \"JsonFieldReplacer\",\n                    \"replace\": \"name\",\n                    \"with\": \"<name prefix> $#date(MM/yy)#$\",\n                    \"function\": {\n                      \"type\": \"JsonArrayResetter\",\n                      \"arrayName\": \"entries\",\n                      \"reset\": {\n                        \"done\" : false,\n                        \"executedAt\": \"\",\n                        \"executedBy\": \"scheduler\",\n                        \"executor\": { \"name\": \"\", \"_id\": \"\" }\n                      }\n                    }\n                  }\n                }\n              }\n            }\n\n          ]\n        }\n      ]\n    }\n  ]\n}"

    val srcList = for {i <- 0 until srcJson.size} yield srcJson.getJsonObject(i)
    val actions = srcList.map(f => {
      val checklist_name = f.getString("checkliste")
      val client_name = f.getString("client")
      val schedule = f.getString("schedule")
      val id = f.getString("id")
      val name_prefix = f.getString("name_prefix")

      val newTarget = target
        .replace("<name checklist>", checklist_name)
        .replace("<scheduled time>", CronExpressionDescriptor.getDescription(schedule, Locale.ENGLISH))
        .replace("<name client>", client_name)
        .replace("<id>", id)
        .replace("<name prefix>", name_prefix)
        .replace("<schedule>", schedule)
        .replace("\"<meta>\"", f.encode)

      new JsonObject(newTarget)
    })

    val targetArray = actions.foldLeft(new JsonArray)((prev, el) => prev.add(el))

    println(targetArray.encodePrettily())
  }

  "Function" should "called with several parameters" in {
    val fT = func(true)
    val fF = func(false)

    fT("SHOULD BE TRUE")
    fF("SHOULD BE FALSE")
  }

  val func:(Boolean) => (String) => Unit = (b) => (str) => {
    println(b)
    println(str)
  }
}
