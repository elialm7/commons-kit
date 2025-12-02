# **Commons Kit**

**Commons Kit** is a robust Java library designed to eliminate boilerplate code and handle common runtime exceptions gracefully. It focuses on four main areas: Functional Error Handling, Safe JSON Manipulation, Financial Math Operations, and Universal Date Conversion.

## **📦 Installation**

clone the repository and do a mvn install in order to use at the moment.

## **📚 Complete API Reference**

### **1\. ErrorUtils (Result\<E, V\>)**

A container for handling success (ok) and failure (err) without exceptions.

#### **A. Factory Methods (Creation)**

| Method | Description | Example |
| :---- | :---- | :---- |
| ok(V) | Creates a Success. | Result.ok("Success"); |
| err(E) | Creates a Failure. | Result.err("Error"); |
| of(Supplier) | Wraps code, catches Exception. | Result.of(() \-\> Integer.parseInt(str)); |
| of(Supplier, Mapper) | Wraps code, maps Exception. | Result.of(() \-\> task(), ex \-\> "Failed: " \+ ex); |
| sequence(List) | List\<Result\> → Result\<List\>. | Result.sequence(listOfResults); |
| flatten(Result) | Unwraps nested Result. | Result.flatten(nestedResult); |
| fromFuture(Future) | Async Future → Result. | Result.fromFuture(completableFuture, ex \-\> "Err"); |

#### **B. Query & State**

| Method | Description | Example |
| :---- | :---- | :---- |
| isOk() | True if Success. | if (res.isOk()) ... |
| isErr() | True if Failure. | if (res.isErr()) ... |
| contains(V) | Checks Success value equality. | res.contains(42); |
| containsErr(E) | Checks Failure error equality. | res.containsErr("Not Found"); |

#### **C. Transformation**

| Method | Description | Example |
| :---- | :---- | :---- |
| map(Fn) | Transforms value. | res.map(str \-\> str.toUpperCase()); |
| flatMap(Fn) | Chains Result operations. | res.flatMap(id \-\> findUser(id)); |
| mapErr(Fn) | Transforms error. | res.mapErr(err \-\> "Error code: " \+ err); |
| bimap(Fn, Fn) | Transforms both sides. | res.bimap(err \-\> "E:" \+ err, val \-\> "V:" \+ val); |
| zip(Result, BiFn) | Combines two Results. | resA.zip(resB, (a, b) \-\> a \+ b); |

#### **D. Validation**

| Method | Description | Example |
| :---- | :---- | :---- |
| ensure(Pred, E) | Validates value. | res.ensure(n \-\> n \> 0, "Must be positive"); |
| filter(Pred, E) | Alias for ensure. | res.filter(n \-\> n \> 0, "Must be positive"); |
| filterNot(Pred, E) | Inverse validation. | res.filterNot(n \-\> n \== 0, "Cannot be zero"); |

#### **E. Side Effects**

| Method | Description | Example |
| :---- | :---- | :---- |
| peek(Cons) | Action on Success. | res.peek(val \-\> log.info(val)); |
| peekErr(Cons) | Action on Failure. | res.peekErr(err \-\> log.error(err)); |
| tap(Cons, Cons) | Action on either. | res.tap(val \-\> log(val), err \-\> log(err)); |
| ifOk(Cons) | Terminal action (void). | res.ifOk(val \-\> System.out.println(val)); |
| ifErr(Cons) | Terminal action (void). | res.ifErr(err \-\> System.err.println(err)); |

#### **F. Recovery**

| Method | Description | Example |
| :---- | :---- | :---- |
| recover(Fn) | Error → Success Value. | res.recover(err \-\> "Default"); |
| or(Supplier) | Returns alternative Result. | res.or(() \-\> Result.ok("Fallback")); |
| orElse(Result) | Returns alternative Result. | res.orElse(Result.ok("Fallback")); |

#### **G. Extraction (Terminal)**

| Method | Description | Example |
| :---- | :---- | :---- |
| getOrElse(V) | Value or Default. | res.getOrElse("Unknown"); |
| getOrElseGet(Supp) | Value or Lazy Default. | res.getOrElseGet(() \-\> complexLogic()); |
| fold(Fn, Fn) | Map both to one type. | res.fold(err \-\> 500, val \-\> 200); |
| toOptional() | Optional\<V\>. | res.toOptional(); |
| toFuture() | CompletableFuture\<V\>. | res.toFuture(); |

#### **💡 Complete Scenario: User Registration Pipeline**
```java
public String registerUser(String rawJson) {
    return Result
        .of(() -> parseJson(rawJson))                        // 1. Wrap potentially failing parsing logic
        .map(json -> json.get("username").trim())            // 2. Transform: extract and clean data
        .ensure(name -> name.length() > 3, "Name too short") // 3. Validate business rule
        .peek(name -> log.info("Registering: " + name))      // 4. Side effect: logging
        .flatMap(name -> saveToDatabase(name))               // 5. Chain another operation that returns Result
        .recover(err -> "Guest_User")                        // 6. Recovery: Fallback if anything above failed
        .fold(
            error -> "Registration Failed: " + error,        // 7. Final Output: Handle Error case
            user  -> "Welcome " + user                       // 8. Final Output: Handle Success case
        );
}
```


### **2\. JsonUtils**

Static utilities for JSON processing using Jackson.

#### **A. Serialization & Parsing**

| Method | Description | Example |
| :---- | :---- | :---- |
| toJson(Obj) | Serializes object to JSON string. | JsonUtils.toJson(user); |
| fromJson(Str, Class) | Deserializes JSON string. | JsonUtils.fromJson(json, User.class); |
| convert(Obj, Class) | Type Alchemy (Map ↔ POJO). | JsonUtils.convert(map, User.class); |
| toMap(Str) | Parses JSON to Map. | JsonUtils.toMap(jsonStr); |
| toList(Str) | Parses JSON Array to List of Maps. | JsonUtils.toList(jsonArrStr); |

#### **B. Tree Operations**

| Method | Description | Example |
| :---- | :---- | :---- |
| toNode(Obj) | Converts Object to Node tree. | JsonUtils.toNode(user); |
| parseNode(Str) | Parses JSON string to Node tree. | JsonUtils.parseNode(jsonStr); |
| stream(Node) | Streams Array elements. | JsonUtils.stream(arrayNode); |

#### **C. Safe Navigation**

| Method | Description | Example |
| :---- | :---- | :---- |
| getString(Node, Path) | Safely gets nested String value. | JsonUtils.getString(node, "user.addr.city"); |

#### **D. Modification**

| Method | Description | Example |
| :---- | :---- | :---- |
| updatePath(Node, Path, Val) | Deep updates or creates nodes. | JsonUtils.updatePath(node, "meta.ver", "1"); |
| merge(Main, Update) | Deep merges two objects. | JsonUtils.merge(defaultConfig, userConfig); |
| prune(Node) | Removes nulls, empty strings/arrays. | JsonUtils.prune(dirtyNode); |
| setProvider(Provider) | Swaps JSON implementation. | JsonUtils.setProvider(new GsonProvider()); |

#### **💡 Complete Scenario: Configuration Manager**
```java
public void loadConfig() {
    String defaults  = "{\"app\": {\"theme\": \"light\", \"retries\": 3}}";
    String overrides = "{\"app\": {\"theme\": \"dark\", \"debug\": null}}";

    // 1. Parse both configs
    JsonNodeWrapper defNode  = JsonUtils.parseNode(defaults).getOrThrow();
    JsonNodeWrapper userNode = JsonUtils.parseNode(overrides).getOrThrow();

    // 2. Merge and prune (remove null 'debug' field)
    JsonNodeWrapper finalConfig = JsonUtils.merge(defNode, userNode)
        .map(JsonUtils::prune)
        .getOrThrow();

    // 3. Modify runtime values
    JsonUtils.updatePath(finalConfig, "app.lastLoaded", System.currentTimeMillis());

    // 4. Safe extraction
    String theme = JsonUtils.getString(finalConfig, "app.theme").orElse("light");
    int retries  = finalConfig.at("/app/retries").asInt();  // JSON Pointer syntax
}
```


### **3\. MathUtils (NumberUtils)**

Null-safe BigDecimal operations. **Inputting null is safe and treated as Zero.**

#### **A. Factory**

| Method | Description | Example |
| :---- | :---- | :---- |
| safeOf(Obj) | Bulletproof converter (Handles nulls, $, etc). | NumberUtils.safeOf("$1,234.56");\\ |

#### **B. Comparison**

| Method | Description | Example |
| :---- | :---- | :---- |
| isPos(Val) | Checks if \> 0\. | NumberUtils.isPos(total); |
| isNeg(Val) | Checks if \< 0\. | NumberUtils.isNeg(balance); |
| isZero(Val) | Checks if \== 0 (null is zero). | NumberUtils.isZero(discount); |
| isEq(A, B) | Checks numerical equality (ignores scale). | NumberUtils.isEq(val, "1.00"); |
| isGt(A, B) | Checks if A \> B. | NumberUtils.isGt(price, limit); |
| isGte(A, B) | Checks if A \>= B. | NumberUtils.isGte(age, 18); |
| inRange(Val, Min, Max) | Checks if Min \<= Val \<= Max. | NumberUtils.inRange(score, 0, 100); |

#### **C. Arithmetic**

| Method | Description | Example |
| :---- | :---- | :---- |
| add(Vals...) | Sums values (nulls treated as 0). | NumberUtils.add(a, b, c); |
| sub(Vals...) | Sequential subtraction. | NumberUtils.sub(total, discount); |
| mul(Vals...) | Product (null results in 0). | NumberUtils.mul(qty, price); |
| div(A, B) | Safe Division (returns 0 if B is 0). | NumberUtils.div(total, count); |
| min(Vals...) | Returns smallest non-null value. | NumberUtils.min(a, b); |
| max(Vals...) | Returns largest non-null value. | NumberUtils.max(a, b); |
| clamp(Val, Min, Max) | Restricts value to range. | NumberUtils.clamp(val, 0, 100); |

#### **D. Utilities**

| Method | Description | Example |
| :---- | :---- | :---- |
| round(Val, Dec) | Rounds HALF\_UP. | NumberUtils.round(val, 2); |
| roundUp(Val, Dec) | Rounds Away from Zero (Ceiling). | NumberUtils.roundUp(val, 0); |
| roundDown(Val, Dec) | Rounds Towards Zero (Floor). | NumberUtils.roundDown(val, 0); |
| percentage(Base, Pct) | Calculates (Base \* Pct / 100). | NumberUtils.percentage(100, 20); |
| extractDigits(Str) | Removes non-digit chars. | NumberUtils.extractDigits("(555) 123"); |

#### **💡 Complete Scenario: Invoice Calculation**
```java
public void calculateInvoice() {
    // 1. Safe input parsing (handles symbols, whitespace, nulls)
    BigDecimal price    = NumberUtils.safeOf("$ 49.99");
    BigDecimal quantity = NumberUtils.safeOf("3");
    BigDecimal taxRate  = NumberUtils.safeOf("8.5"); // 8.5%

    // 2. Arithmetic (price * qty)
    BigDecimal subtotal = NumberUtils.mul(price, quantity);

    // 3. Percentage (subtotal * taxRate / 100)
    BigDecimal tax = NumberUtils.percentage(subtotal, taxRate);

    // 4. Total (subtotal + tax)
    BigDecimal total = NumberUtils.add(subtotal, tax);

    // 5. Validation & output
    if (NumberUtils.isPos(total)
        && NumberUtils.inRange(total, BigDecimal.ZERO, new BigDecimal("1000"))) {

        // Output rounded to 2 decimals: "Total: 162.72"
        System.out.println("Total: " + NumberUtils.round(total, 2));
    }
}
```

### **4\. TimeUtils (DateUtils)**

Universal converter for dates.

#### **A. Parsing**

| Method | Description | Example |
| :---- | :---- | :---- |
| analyze(Str) | Returns Metadata (ambiguity check). | DateUtils.analyze("05/02/2024"); |
| smartParse(Str) | Directly returns LocalDate. | DateUtils.smartParse("2024-03-15"); |

#### **B. Universal Conversion**

| Method | Description | Example |
| :---- | :---- | :---- |
| toLocalDate(Obj) | Converts ANY type to LocalDate. | DateUtils.toLocalDate(legacyDate); |
| toLocalDateTime(Obj) | Converts ANY type to LocalDateTime. | DateUtils.toLocalDateTime(timestamp); |
| toZonedDateTime(Obj, Zone) | Converts to specific Zone. | DateUtils.toZonedDateTime(date, tokyoZone); |
| toUTC(Obj) | Converts to UTC. | DateUtils.toUTC(date); |

#### **C. Manipulation & Logic**

| Method | Description | Example |
| :---- | :---- | :---- |
| atStartOfDay(Obj) | Sets time to 00:00:00. | DateUtils.atStartOfDay(date); |
| atEndOfDay(Obj) | Sets time to 23:59:59. | DateUtils.atEndOfDay(date); |
| withTime(Obj, Str) | Sets specific time. | DateUtils.withTime(date, "14:30"); |
| isWeekend(Obj) | True if Sat/Sun. | DateUtils.isWeekend(date); |
| isBusinessDay(Obj) | True if Mon-Fri. | DateUtils.isBusinessDay(date); |
| daysBetween(A, B) | Absolute days difference. | DateUtils.daysBetween(start, end); |
| format(Temp, Pat) | Safe formatting. | DateUtils.format(date, "yyyy-MM"); |

#### **💡 Complete Scenario: Meeting Scheduler**
```java
public void scheduleMeeting(String userInput) {
    // 1. Analyze input (e.g., "15/03/2024")
    LocalDate date = DateUtils.analyze(userInput)
        .map(ParsedDate::date)
        .getOrElse(LocalDate.now());

    // 2. Business logic: move weekends to Monday
    if (DateUtils.isWeekend(date)) {
        date = date.plusDays(2);
    }

    // 3. Normalization: set constraints (start/end times)
    LocalDateTime start = DateUtils.withTime(date, "09:00");
    LocalDateTime end   = DateUtils.withTime(date, "17:00");

    // 4. Global storage: convert to UTC
    ZonedDateTime utcStart = DateUtils.toUTC(start).getOrThrow();

    System.out.printf(
        "Meeting set for %s (UTC: %s)%n",
        DateUtils.format(start, "yyyy-MM-dd HH:mm"),
        utcStart
    );
}
```
