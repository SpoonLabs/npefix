import csv

counterPerLocation = {}

total = 0

with open('instrumentation-output.csv', 'rb') as csvfile:
	mutations_csv = csv.reader(csvfile, delimiter='\t', quotechar='|')
	for row in mutations_csv:
		location = row[0] + ':' + row[2]
		if not location in counterPerLocation:
			counterPerLocation[location] = [0,0]
		if row[1] == 'false':
			counterPerLocation[location][1] += 1
		else:
			counterPerLocation[location][0] += 1
		total += 1

print total, " total number of executions"

listNotDiverse=[]
listDiverse=[]

for record in counterPerLocation:
	if counterPerLocation[record][0] == 0 or \
		counterPerLocation[record][1] == 0:
		listNotDiverse.append(record)
	else:
		listDiverse.append(record)

print len(listDiverse), " number of location that have both values"
acc = 0
for record in listDiverse:
	print str(counterPerLocation[record][0] + counterPerLocation[record][1]), record, str(counterPerLocation[record])
	acc += counterPerLocation[record][0] + counterPerLocation[record][1]
print acc, " total number of execution with both values"

print "=" * 30

print len(listNotDiverse), " number of location with single value"
acc = 0
for record in listNotDiverse:
	print str(counterPerLocation[record][0] + counterPerLocation[record][1]),  record, str(counterPerLocation[record])
	acc += counterPerLocation[record][0] + counterPerLocation[record][1]
print acc, " total number of execution with single value"

