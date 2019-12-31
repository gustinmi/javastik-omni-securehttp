
## Prepare test webservice

This demo requires SoapUI program, which can be freely downloaded from [https://www.soapui.org/downloads/soapui.html] 
Once program it's installed, you must configure SSL, open project start mock services.

## Consuming webservice with CURL

SOAP Webservice call 

'''
curl -v -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:are="http://tempuri.org/AreaService/"> <soapenv:Header/> <soapenv:Body> <are:parameters> <width>1</width> <height>1</height> </are:parameters> </soapenv:Body> </soapenv:Envelope>' --header "content-type: application/soap+xml" "http://localhost:8088/mockAreaServiceSOAP"
'''

SOAP Webservice call to SSL server

 *  curl "https://localhost:8443/mockAreaServiceSOAP" -v --insecure -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:are="http://tempuri.org/AreaService/"> <soapenv:Header/> <soapenv:Body> <are:parameters> <width>1</width> <height>1</height> </are:parameters> </soapenv:Body> </soapenv:Envelope>' 
 