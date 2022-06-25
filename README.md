# useful code-blocks


```java

        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        tokData.getString("id_U"),
        tokData.getString("id_C"),
```

``` java

        Order order = coupaUtil.getOrderByListKey(id_O, Arrays.asList("info","oItem", "action"));



```

``` java
        LogFlow logL = new LogFlow(logType,id_FC,
                    id_FS,"stateChg", id_U,grpU,orderOItem.getId_P(),orderOItem.getGrpB(),orderOItem.getGrp(),
                    id_O,index,id_C,orderOItem.getId_C(), "",dep,message,3,orderOItem.getWrdN(),wrdNU);
            
            logL.setLogData_action(orderAction,orderOItem);
        
            wsClient.sendWS(logL);
    
            logUtil.sendLog(logL.getLogType(),JSON.toJSONString(logL));

```