package com.translate.wublub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


class JsonParse {

    private StringBuilder parseResult = new StringBuilder();

    public String parse(String jsonText){

        try{
            JSONObject dataJson=new JSONObject(jsonText);

            parseResult.setLength(0);

            /* 最基本的翻译结果 */
            if (dataJson.has("translation")){
                parseResult.append("翻译结果：");
                JSONArray translationArr = dataJson.getJSONArray("translation");
                parseResult.append(translationArr.getString(0));
                for (int i = 1; i < translationArr.length(); ++i){
                    parseResult.append(", " + translationArr.getString(i));
                }
                parseResult.append("\n\n");
            }

            /* 词典释义  */
            if (dataJson.has("basic")){
                parseResult.append("词典释义：\n");
                JSONObject basicObj = dataJson.getJSONObject("basic");
                if (!basicObj.has("us-phonetic") && !basicObj.has("uk-phonetic") && basicObj.has("phonetic")){
                    // 如果以上条件成立，则为中文拼音
                    parseResult.append("[" + basicObj.getString("phonetic") + "]\n");
                }else{
                    // 否则为英文音标
                    if (basicObj.has("us-phonetic")){
                        parseResult.append("美[" + basicObj.getString("us-phonetic") + "], ");
                    }
                    if (basicObj.has("uk-phonetic")){
                        parseResult.append("英[" + basicObj.getString("uk-phonetic") + "]\n");
                    }
                }
                if (basicObj.has("explains")){
                    JSONArray basicArr = basicObj.getJSONArray("explains");
                    for (int i = 0; i < basicArr.length(); ++i){
                        parseResult.append(basicArr.getString(i) + "\n");
                    }
                    parseResult.append("\n");
                }
            }

            /* 网络释义 */
            if (dataJson.has("web")){
                parseResult.append("网络释义：\n");
                JSONArray webArr = dataJson.getJSONArray("web");
                JSONObject webObj;
                JSONArray webSubArr;
                for (int i = 0; i < webArr.length(); ++i){
                    webObj = webArr.getJSONObject(i);
                    parseResult.append("(" + (i + 1) + "): ");
                    parseResult.append(webObj.getString("key") + "\n");

                    webSubArr = webObj.getJSONArray("value");
                    parseResult.append(webSubArr.getString(0));
                    for (int j = 1; j < webSubArr.length(); ++j){
                        parseResult.append(", " + webSubArr.getString(j));
                    }
                    parseResult.append("\n");
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        return parseResult.toString();
    }
}
