#bash
_MVN_R=$1
_MVN_W=$2

echo "<settings> 
	<servers> 
	   <server>
              <id>37coins.myMavenRepo.read</id>
              <username>myMavenRepo</username>
              <password>$_MVN_R</password>
   	   </server>
	   <server>
              <id>37coins.myMavenRepo.write</id>
              <username>myMavenRepo</username>
              <password>$_MVN_W</password>
   	   </server>
	</servers>
</settings>" > settings.xml

mv settings.xml ~/.m2/

