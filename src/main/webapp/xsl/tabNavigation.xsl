<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

    <xsl:template name="tabNavigation">
        <xsl:param name="activeTab"/>

        <table class="tabs">
            <tr>
                <td class="tabSpacer" style="min-width:13px;"></td>

                <xsl:if test="$activeTab='files'">
                    <td class="tabActive" resource="label.modelist" />
                </xsl:if>
                <xsl:if test="$activeTab!='files'">
                    <td class="tabInactive">
                        <a class="tab" href="javascript:viewModeList()" resource="label.modelist" />
                    </td>
                </xsl:if>

                <td class="tabSpacer"></td>

                <xsl:if test="$activeTab='thumbnails'">
                    <td class="tabActive" resource="label.modethumb" />
                </xsl:if>
                <xsl:if test="$activeTab!='thumbnails'">
                    <td class="tabInactive">
                        <a class="tab" href="javascript:viewModeThumbs()" resource="label.modethumb" />
                    </td>
                </xsl:if>

                <xsl:if test="/fileList/videoEnabled">

                    <td class="tabSpacer"></td>

                    <xsl:if test="$activeTab='videos'">
                        <td class="tabActive" resource="label.modeVideo" />
                    </xsl:if>
                    <xsl:if test="$activeTab!='videos'">
                        <td class="tabInactive">
                            <a class="tab" href="javascript:viewModeVideo()" resource="label.modeVideo" />
                        </td>
                    </xsl:if>

                </xsl:if>

                <td class="tabSpacer"></td>

                <td class="tabInactive">
                    <a class="tab" href="javascript:viewModeStory()" resource="label.modestory" />
                </td>

                <td class="tabSpacer"></td>

                <td class="tabInactive">
                    <a class="tab" href="javascript:viewModeSlideshow()" resource="label.modeSlideshow" />
                </td>

                <xsl:if test="not(/fileList/readonly) and /fileList/statistics">
                    <td class="tabSpacer"></td>

                    <xsl:if test="$activeTab='stats'">
                        <td class="tabActive" resource="label.fileStats" />
                    </xsl:if>
                    <xsl:if test="$activeTab!='stats'">
                        <td class="tabInactive">
                            <a class="tab" href="javascript:fileStats()" resource="label.fileStats" />
                        </td>
                    </xsl:if>

                </xsl:if>

                <td class="tabSpacer" style="width:90%"></td>
            </tr>
        </table>

    </xsl:template>

</xsl:stylesheet>
