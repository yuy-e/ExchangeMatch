**Developer:** Zijing Zhang, Yue Yu

**Language:** Java

**Description:**

An exchange matching engine that supports matching buy and sell orders for a stock/commodities market. The engine provides functions including accounts creation, positions creation, creating, querying, and canceling orders. 

#Run Command:

```
./gradlew installDist
```
For Server: 
```
./server/build/install/server/bin/server
```

For Client: 
```
./client/build/install/client/bin/client file.xml threadNum  
```
ex:
```
./client/build/install/client/bin/client create_test.xml 100
```
