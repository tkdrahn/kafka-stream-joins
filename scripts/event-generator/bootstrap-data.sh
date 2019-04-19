#!/usr/bin/env bash

curl -s -d '{
	"address": "tim.drahn@objectpartners.com",
	"emailId": "email-1",
	"personId": "person-1",
	"type": "OFFICE"
}' -H "Content-Type: application/json" -X POST localhost:8180/email

curl -s -d '{
	"address": "tim.drahn@personal.com",
	"emailId": "email-2",
	"personId": "person-1",
	"type": "OFFICE"
}' -H "Content-Type: application/json" -X POST localhost:8180/email

curl -s -d '{
	"personId": "person-1",
	"phoneNumber": "123-456-7890",
	"telephoneId": "phone-1",
	"type": "CELL"
}' -H "Content-Type: application/json" -X POST localhost:8180/phone

curl -s -d '{
	"firstName": "Test",
	"lastName": "Person",
	"personId": "phone-1"
}' -H "Content-Type: application/json" -X POST localhost:8180/name


