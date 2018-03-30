import pandas as pd
import os, errno
import sys

# Barre de chargement, nom du fichier de sortie et d'entrée
animation = "|/-\\"
file_output = "./rdf_output.rdf"
file = open(file_output, "w+")
# Charge le csv dans une dataFrame
df = pd.read_csv("sorted_data.csv", sep='\n')

i = 0
node_list = []
# On parcours les lignes de la dataframe
while(i < len(df)):
    sys.stdout.write("\r" + animation[i % len(animation)] + str(i) + "/" + str(len(df)))
    sys.stdout.flush()
    # node_id contient le numero correspondant à la source de chaque ligne
    node_id =  df.iloc[i][0].split(",")[0]
    # la string a insérer dans le fichier rdf qui correspond au sujet du triplet
    subject = '    <foaf:Person rdf:about="http://www.une_uri.org/HMIN209Social.rdf#trader_' + node_id + '">'

    # Si le node_id apparait pour la premiere fois dans la liste, alors on écrit le sujet dans le fichier rdf
    if not node_id in node_list:
        # Cela va générer une ligne en trop au début du fichier, il faudrat penser à la supprimer à la main
        file.write(''.join("    </foaf:Person>"))
        file.write('\n')
        file.write(''.join(subject))
        file.write('\n')
    # Ajoutee le noeud à la liste et change de ligne
    node_list.append(node_id)

    # Maintenant, on s'occupe d'ajouter la bonne propriété en fonction de la note
    target = df.iloc[i][0].split(",")[1]
    note = df.iloc[i][0].split(",")[2]
    if(int(note)<=-5):
        # <wss:collaboratesBadWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_TARGET"/>
        # print("bad")
        file.write(''.join('        <wss:collaboratesBadWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_' + target + '"/>'))
        file.write('\n')
    if(int(note)>=5):
        # <wss:collaboratesWellWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_TARGET"/>
        file.write(''.join('        <wss:collaboratesWellWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_' + target + '"/>'))
        file.write('\n')
    else:
        # <sor:collaboratesWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_TARGET"/>
        file.write(''.join('        <sor:collaboratesWith rdf:resource="http://www.une_uri.org/HMIN209Social.rdf#trader_' + target + '"/>'))
        file.write('\n')

    i = i+1

file.close()
