<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="version"/>
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/extensions/extension[groupId='org.arquillian.smart.testing' and artifactId='maven-lifecycle-extension']/version/text()">
    <xsl:value-of select="$version"/>
  </xsl:template>

</xsl:stylesheet>
