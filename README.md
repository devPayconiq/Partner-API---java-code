# Payconiq Partner API


+ ##### Create a partner account

	Go to  [Payconiq developers portal](https://developer.payconiq.com)  and create a test account.


+ ##### Retrieve partner
    ###### Definition:  
    ```
    'GET' - https://dev.payconiq.com/v1/partners/{id}
    ```
    ###### Example Request:
    ```shell
    curl -X GET -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af"
    "https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3"
    ```
    
+ ##### Register RSA public key
	
  ###### Definition:
   ```
   'PUT' - https://dev.payconiq.com/v1/partners/{id}/key
   ```    
    
  ###### Example Request:
  ```shell
  curl -v -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af" \
  -H "Content-Type: application/json" \
  -X "PUT" \
  -d '{ "value": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwI..." }' \
  "https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/key"
  ```

+ ##### Create a Customer 
	
    > To generate a valid IBAN go to https://www.generateiban.com 
    
    ###### Definition:
    ```
    'POST' - https://dev.payconiq.com/v1/partners/{id}/customers
    ```
    ###### Example Request:
    ```shell
    curl -v -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af" \
    -d '{
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+31612222598",
      "email": "john.doe@customer.com",
      "address": {
        "street": "Orlyplein",
        "no": "85",
        "postalCode": "1043 DS",
        "city": "Amsterdam",
        "country": "NLD"
      },
      "bankAccounts": [
        {
          "IBAN": "NL91ABNA0417164300",
          "name": "ING",
          "mandateReference": "REF0000000000000000000000003", //should be "REF" + 25 hex digits
          "mandateSignDate": "1483228800000"
        }
      ]
    }' \
    https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/customers
	```
        
    >Mandate Management is disabled.
    >This means that Partner is responsible to provide the mandate reference and sign date for any Customer during the onboarding.
    >With this setting Partners will be required to provide the mandateSignDate and mandateReference on Customer's json payload. In
    >that case Partner is responsible about the veracity of this information. Any additional request are necessary. After this a
    >Customer can be used on transactions.

+ ##### Create a Transaction

   To sign the transactions convert private key into a DER private key
   ```shell
   $ openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
   ```
   If you want to verify the signature with the public key, convert the public key into a DER public key
   ```shell
   $ openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der
   ```

   ###### Definition
   ```
   'POST' - https://dev.payconiq.com/v1/partners/{id}/transactions
   ```

   ##### SDD Payment
   If you want to create an SDD transaction signature between the partner and the customer

    ```java
    //Code to generate SDD transaction signature
    String partnerId = "58961529445edf0001fbb2b3", senderId = "589618f198fff10001106fc7",
           senderIBAN = "NL91ABNA0417164300", currency = "EUR", amount = "10";

    StringBuilder builder = new StringBuilder();
    builder.append(partnerId).append(senderId).
            append(senderIBAN).append(amount).append(currency);

    PrivateKey privateKey = getPrivateKey(privateKeyPath); //in DER format
    Signature sig = Signature.getInstance("SHA256WithRSA");
    sig.initSign(privateKey);
    sig.update(data.getBytes("UTF-8"));
    String signature = DatatypeConverter.printBase64Binary(sig.sign());
    ```

    ###### Example Request
    ```shell
    curl -v -X POST -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af" \
      -d '{
      "originId": "589618f198fff10001106fc7",
      "originIBAN": "NL91ABNA0417164300",
      "amount": "10",
      "currency": "EUR",
      "signature":"<generated-signature>"
      }' https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/transactions
	```

    ##### P2P Payment
    If you want to create a transaction signature between 2 customers

    ```java
    //Code to generate P2P transaction signature
    String partnerId = "58961529445edf0001fbb2b3", senderId = "589618f198fff10001106fc7",
           senderIBAN = "NL91ABNA0417164300", recipientId = "5896190598fff10001106fc8",
           recipientIBAN = "NL02ABNA0457180536", currency = "EUR", amount = "10";

    StringBuilder builder = new StringBuilder();
    builder.append(partnerId).append(senderId).
            append(senderIBAN).append(amount).append(currency);

    PrivateKey privateKey = getPrivateKey(privateKeyPath);  //in DER format
    Signature sig = Signature.getInstance("SHA256WithRSA");
    sig.initSign(privateKey);
    sig.update(data.getBytes("UTF-8"));
    String signature = DatatypeConverter.printBase64Binary(sig.sign());
    ```

    ###### Example Request
    ```shell
    curl -v -X POST -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af" \
      -d '{
      "originId": "589618f198fff10001106fc7",
      "originIBAN": "NL91ABNA0417164300",
      "targetId": "5896190598fff10001106fc8",
      "targetIBAN": "NL02ABNA0457180536",
      "amount": "10",
      "currency": "EUR",
      "signature":"<generated-signature>"
      }' https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/transactions
    ```

	##### SCT Payment
    If you want to create an SCT transaction signature between the partner and the customer

    ```java
    //Code to generate SCT transaction signature
    String partnerId = "58961529445edf0001fbb2b3", senderId = "58961529445edf0001fbb2b3",
    senderIBAN = "NL55INGB0000000000", recipientId = "5896190598fff10001106fc8",
    recipientIBAN = "NL02ABNA0457180536", currency = "EUR", amount = "10";

    StringBuilder builder = new StringBuilder();
    builder.append(partnerId).append(senderId)
        .append(senderIBAN).append(recipientId)
        .append(recipientIBAN).append(amount).append(currency);

    PrivateKey privateKey = getPrivateKey(privateKeyPath);  //in DER format
    Signature sig = Signature.getInstance("SHA256WithRSA");
    sig.initSign(privateKey);
    sig.update(data.getBytes("UTF-8"));
    String signature = DatatypeConverter.printBase64Binary(sig.sign());
    ```

    ###### Example Request
    ```shell
    curl -v -X POST -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af" \
	  -d '{
      "originId": "58961529445edf0001fbb2b3",
      "originIBAN": "NL55INGB0000000000",
      "targetId": "5896190598fff10001106fc8",
      "targetIBAN": "NL02ABNA0457180536",
      "amount": "10",
      "currency": "EUR",
      "signature":"<generated-signature>"
      }' \
	  https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/transactions
	```

+ ##### Retrieve the Transaction

  After creating the transaction take the url of the location header 
  (**i.e: location: https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/transactions/5899b942ab9bed000125295f**) and use a GET operation with the Authorization token to get the information regarding the transaction that was created in the previous step.
###### Definition:
	
	```
	'GET' - https://dev.payconiq.com/v1/partners/{id}/transactions/{transaction_id}
	```
	
	###### Example Request:
	```shell
    	
	curl -X GET -H "Content-Type: application/json" -H "Authorization: 988b0157-f3bd-47ea-a885-4cae37cd37af"
    	"https://dev.payconiq.com/v1/partners/58961529445edf0001fbb2b3/transactions/58999c7e98fff10001106ffd"
    	
	```
