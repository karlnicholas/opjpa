<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="emailInformation">
<html>
<head/>
<body>
<p><xsl:value-of select="email" /></p>
<p>New Opinions at Court Reports</p>
<table>
  <tr bgcolor="#9acd32">
    <th>Date</th>
    <th>Title</th>
    <th>Filename</th>
  </tr>
  <xsl:for-each select="opinionCases">
    <tr>
      <td><xsl:value-of select="opinionDate" /></td>
      <td><xsl:value-of select="title" /></td>
      <td><xsl:value-of select="fileName" /></td>
    </tr>
    <tr>
    <td><xsl:text> </xsl:text></td>
    <td><xsl:text> </xsl:text>
        <xsl:for-each select="sectionViews">
          <xsl:value-of select="displayTitlePath" /><xsl:text>  </xsl:text><xsl:value-of select="displaySections" /><br />
        </xsl:for-each>
      </td>
    <td><xsl:text> </xsl:text></td>
    </tr>
  </xsl:for-each>
</table>
<p>Regards,<br /><br />Court Opinions.</p>
</body>
</html>
</xsl:template>
</xsl:stylesheet>