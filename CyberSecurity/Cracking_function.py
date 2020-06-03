import sys
import os
from string import ascii_uppercase

def function2(var):
	for n1 in range(0,10):
		for n2 in range(0,10):
			for n3 in range(0,10):
				for n4 in range(0,10):
					for a in ascii_uppercase:
						for b in ascii_uppercase:
							for c in ascii_uppercase:
								sn1 = str(n1)
								sn2 = str(n2)
								sn3 = str(n3)
								sn4 = str(n4)
								print(a+b+c+sn1+sn2+sn3+sn4)
								output_from_os = os.popen("echo -n"+a+b+c+sn1+sn2+sn3+sn4+"|openssl dgst -hex -sha256")
								current_hash = output_from_os.read().split()[1]
								if(var==current):
									print(current)
									sys.exit("Answer: "+a+b+c+sn1+sn2+sn3+sn4)

def function1():
	var = input("please enter password with format uuudddd, like ABC1234: ")
	output_from_os = os.popen("echo -n"+a+b+c+sn1+sn2+sn3+sn4+"|openssl dgst -hex -sha256")
	curren_hash = output_from_os.read().split()[1]
	print("digested password: " + current_hash)
	return current

variable = function1()
function2(variable)
