import random

total = 0
n = 0
for i in range(100000):
	total -= 1
	total += random.randint(0,2)
	n += 1
print (total, n)
print (total/n)