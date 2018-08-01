package com.naivebayes.core;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author rsriramakavacham
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OilRefinaryAttributes {
	
	private long CrudeOil ;
	private long Temperature ;
	private long AnilinePoint ;
	private String APIgravity ;
	private String SulphurContent ;
	private String Viscosity;
	private String ReidVaporPressure ;
	private String AsphaltenesCarbonResidue ;
	private String OilQuality ;
	private String OilRefinaryType ;
	private String RefinaryName ;
	
	
	
	public String getRefinaryName() {
		return RefinaryName;
	}
	@Override
	public String toString() {
		return "OilRefinaryAttributes [CrudeOil=" + CrudeOil + ", Temperature=" + Temperature + ", AnilinePoint="
				+ AnilinePoint + ", APIgravity=" + APIgravity + ", SulphurContent=" + SulphurContent + ", Viscosity="
				+ Viscosity + ", ReidVaporPressure=" + ReidVaporPressure + ", AsphaltenesCarbonResidue="
				+ AsphaltenesCarbonResidue + ", OilQuality=" + OilQuality + ", OilRefinaryType=" + OilRefinaryType
				+ ", RefinaryName=" + RefinaryName + "]";
	}
	public void setRefinaryName(String refinaryName) {
		RefinaryName = refinaryName;
	}
	public long getCrudeOil() {
		return CrudeOil;
	}
	public void setCrudeOil(long crudeOil) {
		CrudeOil = crudeOil;
	}
	public long getTemperature() {
		return Temperature;
	}
	public void setTemperature(long temperature) {
		Temperature = temperature;
	}
	public long getAnilinePoint() {
		return AnilinePoint;
	}
	public void setAnilinePoint(long anilinePoint) {
		AnilinePoint = anilinePoint;
	}
	
	public String getAPIgravity() {
		return APIgravity;
	}
	public void setAPIgravity(String aPIgravity) {
		APIgravity = aPIgravity;
	}
	public String getSulphurContent() {
		return SulphurContent;
	}
	public void setSulphurContent(String sulphurContent) {
		SulphurContent = sulphurContent;
	}
	public String getViscosity() {
		return Viscosity;
	}
	public void setViscosity(String viscosity) {
		Viscosity = viscosity;
	}
	public String getReidVaporPressure() {
		return ReidVaporPressure;
	}
	public void setReidVaporPressure(String reidVaporPressure) {
		ReidVaporPressure = reidVaporPressure;
	}
	public String getAsphaltenesCarbonResidue() {
		return AsphaltenesCarbonResidue;
	}
	public void setAsphaltenesCarbonResidue(String asphaltenesCarbonResidue) {
		AsphaltenesCarbonResidue = asphaltenesCarbonResidue;
	}
	public String getOilQuality() {
		return OilQuality;
	}
	public void setOilQuality(String oilQuality) {
		OilQuality = oilQuality;
	}
	public String getOilRefinaryType() {
		return OilRefinaryType;
	}
	public void setOilRefinaryType(String oilRefinaryType) {
		OilRefinaryType = oilRefinaryType;
	}
	
	
	
}
