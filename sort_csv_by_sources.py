import csv

# On trie le document csv par SOURCE en ordre croissant
with open('soc-sign-bitcoinalpha.csv') as csv_data:
    reader = csv.reader(csv_data, delimiter=',')
    number_sorted = sorted(reader, key=lambda x:int(x[0]), reverse=False)

# On écrit le nouveau csv sorted_data.csv
with open('sorted_data.csv', 'w') as csvfile:
    fieldnames = ['SOURCE', 'TARGET', 'RATE']
    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    for line in number_sorted:
        writer.writerow({'SOURCE': line[0], 'TARGET': line[1], 'RATE': line[2]})
