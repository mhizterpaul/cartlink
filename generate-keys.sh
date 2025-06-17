#!/bin/bash

# Create certs directory in user's home directory
mkdir -p ~/.cartlink/certs

# Generate private key
openssl genpkey -algorithm RSA -out ~/.cartlink/certs/private.pem -pkeyopt rsa_keygen_bits:2048

# Generate public key from private key
openssl rsa -pubout -in ~/.cartlink/certs/private.pem -out ~/.cartlink/certs/public.pem

echo "RSA key pair generated successfully in ~/.cartlink/certs/" 