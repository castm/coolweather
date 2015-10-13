package model;

public class Province {

	private int id;
	private String provinceName;
	private String provinceCode;
	
	public int getId(){
		return id;
	}
	public void setId(int i){
		this.id=id;
	}
	public String getProvinceNmae(){
		return provinceName;
	}
	public void setProvionceName(String provinceName){
		this.provinceName=provinceName;
	}
	public String getProvinceCode(){
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode){
		this.provinceCode=provinceCode;
	}
}
