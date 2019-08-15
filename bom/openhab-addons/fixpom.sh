while read bundle
do
    cat <<hei
    <dependency>
      <groupId>org.openhab.addons.bundles</groupId>
      <artifactId>$bundle</artifactId>
      <version>\${project.version}</version>
    </dependency>
hei
done < ../../bundles/updated.txt
