#!/usr/bin/env bash

curl -s -d '{
	"key": {
	    "personId": "person-1",
        "type": "ALTERNATE"
	},
	"value": {
		"address": "tim.drahn@alternate.com"
	}
}' -H "Content-Type: application/json" -X POST localhost:8180/email

curl -s -d '{
	"key": {
	    "personId": "person-1",
        "type": "HOME"
	},
	"value": {
		"address": "tim.drahn@personal.com"
	}
}' -H "Content-Type: application/json" -X POST localhost:8180/email

curl -s -d '{
	"key": {
	    "personId": "person-1",
        "type": "OFFICE"
	},
	"value": {
		"address": "tim.drahn@objectpartners.com"
	}
}' -H "Content-Type: application/json" -X POST localhost:8180/email

curl -s -d '{
	"key": {
	    "personId": "person-1",
	    "type": "CELL"
	},
	"value": {
		"phoneNumber": "123-456-7890"
	}
}' -H "Content-Type: application/json" -X POST localhost:8180/phone

curl -s -d '{
	"key": {
	    "personId": "person-1"
	},
	"value": {
	    "firstName": "Firstname",
	    "lastName": "Lastname"
	}
}' -H "Content-Type: application/json" -X POST localhost:8180/name
