import math
import random
import os
import sys

#client

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
	var = input("please enter the public key for alice:")
	return var

def calculate_shared_secret(alices_public_key, bobs_private_key, q):
	secret = math.pow(alices_public_key, bobs_private_key) % q
	return secret

def decrypt_file(file_to_decrypt):
	os.popen("openssl base64 -d -in "+file_to_decrypt+" -out decrypted_file.txt")

def rc4_decrypt(file_to_decrypt, key_to_use):
	bytes = []
	fil = open(file_to_decrypt, 'r') # convert hexadecimal to bytes
	hex_str = fil.read()
	for i in range(0, len(hex_str), 2):
		byte = hex_str[i:i+2]
		bytes.append(int('0X' + byte, 16))
	fil.close()
	cipherBytes = bytes


	bytes = []
	fil = open(key_to_use, 'r') # convert text to bytes
	s = fil.read()
	fil.close()
	for byte in s:
		bytes.append(ord(byte))
	keyBytes = bytes
	
	keyStream = [] # key-scheduling
	ciperList = []
	keyLen = len(keyBytes)
	cipherLen = len(cipherBytes)
	S = range(256)	
	j = 0
	for i in range(256):
		j = (j+S[i] + keyBytes[i % keyLen]) % 256
		S[i] = S[j]
		S[j] = S[i]
	i = 0
	j = 0
	for a in range(cipherLen): # RC4 algorithm
		i = (i + 1) % 256
		j = (j + S[i]) % 256
		S[i] = S[j]
		S[j] = S[i]
		K = S[(S[i] + S[j]) % 256]
		keyStream.append(K)
		ciperList.append(K^cipherBytes[a])

	fil = open("keystream2.txt", 'w') #writing output in hexadecimal
	for byte in keyStream:
		hex_str = '0' + hex(byte)[2:]
		fil.write(hex_str[-2:].upper())
	fil.close()

	string = ''
	for byte in ciperList:
		s += chr(byte)
	fil = open("index1.html", 'w') # converting output from bytes to text
	fil.write(s)
	fil.close()

q = set_public_parameters_q()
alpha = set_public_parameters_alpha()
a_private_key = 5#calculate_private_key(q, alpha)
a_public_key = calculate_public_key(q, alpha)
b_public_key = ask_for_public_key()
shared_secret = int(calculate_shared_secret(b_public_key, a_private_key, q))
shared_secret_str = str(shared_secret)
print("shared secret Kab = " + shared_secret_str)
k2 = os.popen("echo -n"+shared_secret_str+"|openssl dgst -sha256")
k2 = k2.read().split()[1]
print("K2 = " + k2)
fil = open('enc_key.txt', 'w')
fil.write(k2)
fil.close()
decrypt_file("encrypted_file.txt")
rc4_decrypt("decrypted_file.txt", "enc_key.txt")
