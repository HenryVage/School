import math
import random
import os
import sys
#server

def set_public_parameters_q():
	q = input("please enter q: ")
	return q

def set_public_parameters_alpha():
	alpha = input("please enter alpha: ")
	return alpha

def calculate_private_key(q, alpha):
	private_key = random.randint(1, q) #selecting private key, not using a secure random number generator here
	return private_key

def calculate_public_key(q, alpha):
	private_key = random.randint(1, q) #selecting private key, not using a secure random number generator here
	public_key = math.pow(alpha, private_key) % q
	return public_key

def ask_for_public_key():
	var = input("please enter the public key for bob:")
	return var
	
def calculate_shared_secret(bobs_public_key, alices_private_key, q):
	secret = math.pow(bobs_public_key, alices_private_key) % q
	return secret

def encrypt_file(file_to_encrypt):
	os.popen("openssl base64 -e -in "+file_to_encrypt+" -out encrypted_file.txt")
	
def rc4_encrypt(file_to_encrypt, key_to_use):
	bytes = []
	fil = open(file_to_encrypt, 'r') #convert text to bytes
	s = fil.read()
	fil.close()
	for byte in s:
		bytes.append(ord(byte))
	fileBytes = bytes

	bytes = []
	fil = open(key_to_use, 'r') #convert text to bytes
	s = fil.read()
	fil.close()
	for byte in s:
		bytes.append(ord(byte))
	keyBytes = bytes
	
	keyStream = [] # Key-scheduling
	ciperList = []
	keyLen = len(keyBytes)
	fileLen = len(fileBytes)
	S = range(256)	
	j = 0
	for i in range(256):
		j = (j+S[i] + keyBytes[i % keyLen]) % 256
		S[i] = S[j]
		S[j] = S[i]

	i = 0
	j = 0
	for a in range(fileLen): # RC4 algorithm
		i = (i + 1) % 256
		j = (j + S[i]) % 256
		S[i] = S[j]
		S[j] = S[i]
		K = S[(S[i] + S[j]) % 256]
		keyStream.append(K)
		ciperList.append(K^fileBytes[a])

	fil = open("keystream.txt", 'w') # writing output in hexadecimal
	for byte in keyStream:
		hex_str = '0' + hex(byte)[2:]
		fil.write(hex_str[-2:].upper())
	fil.close()

	fil = open("Ciphertext-Q3.txt", 'w') # writing output in hexadecimal
	for byte in ciperList:
		hex_str = '0' + hex(byte)[2:]
		fil.write(hex_str[-2:].upper())
	fil.close()
	
	

q = set_public_parameters_q()
alpha = set_public_parameters_alpha()
a_private_key = 13#calculate_private_key(q, alpha)
a_public_key = calculate_public_key(q, alpha)
b_public_key = ask_for_public_key()
shared_secret = int(calculate_shared_secret(b_public_key, a_private_key, q))
shared_secret_str = str(shared_secret)
print("shared secret Kab = " + shared_secret_str)
k1 = os.popen("echo -n"+shared_secret_str+"|openssl dgst -sha256")
k1 = k1.read().split()[1]
print("K1 = " + k1)
fil = open('enc_key.txt', 'w')
fil.write(k1)
fil.close()
rc4_encrypt("index.html", "enc_key.txt")
encrypt_file("Ciphertext-Q3.txt")
