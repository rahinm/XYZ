/*
Copyright 2020 Mohammad A. Rahin                                                                                                          

Licensed under the Apache License, Version 2.0 (the "License");                                                                           
you may not use this file except in compliance with the License.                                                                          
You may obtain a copy of the License at                                                                                                   
    http://www.apache.org/licenses/LICENSE-2.0                                                                                            
Unless required by applicable law or agreed to in writing, software                                                                       
distributed under the License is distributed on an "AS IS" BASIS,                                                                         
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.                                                                  
See the License for the specific language governing permissions and                                                                       
limitations under the License.       
*/

package net.dollmar.web.apicatalogue.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "API_DEFS")
public class ApiDef implements java.io.Serializable, Comparable {
	
	private static final long serialVersionUID = 1495331023666978971L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apidef_generator")
	@SequenceGenerator(name="apidef_generator", sequenceName = "apidef_seq", initialValue = 1, allocationSize = 20)
	@Column(name = "ID", updatable = false, nullable = false)
	private Long id;
	
	@Column(name = "API_NAME", unique = false)
	private String apiName;
	
  	@Column(name = "API_DESC", unique = false)
	private String apiDesc;
	
	@Column(name = "APIDEF_VERSION", unique = false)
	private String version;

	@Column(name = "API_TYPE", unique = false)
	private String type;

	@Column(name = "API_USAGE", unique = false)
	private String usage;

	@Column(name = "API_STATUS", unique = false)
	private String status;

	@Column(name = "FILE_NAME", unique = false)
	private String fileName;

	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getApiName() {
    	return apiName;
  	}

  	public void setApiName(String apiName) {
    	this.apiName = apiName;
  	}

  	public String getApiDesc() {
    	return apiDesc;
  	}

  	public void setApiDesc(String apiDesc) {
    	this.apiDesc = apiDesc;
  	}

  	public String getVersion() {
    	return version;
  	}
  
  	public void setVersion(String version) {
    	this.version = version;
  	}
    
  	public String getType() {
    	return type;
  	}
  
  	public void setType(String type) {
    	this.type = type;
  	}
    
  	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFileName() {
    	return fileName;
  	}

  	public void setFileName(String fileName) {
    	this.fileName = fileName;
  	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((apiName == null) ? 0 : apiName.hashCode());
		result = prime * result + ((apiDesc == null) ? 0 : apiDesc.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((usage == null) ? 0 : usage.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiDef other = (ApiDef) obj;
		if (apiName == null) {
			if (other.apiName != null)
				return false;
		} else if (!apiName.equals(other.apiName))
			return false;
		if (apiDesc == null) {
			if (other.apiDesc != null)
				return false;
		} else if (!apiDesc.equals(other.apiDesc))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
	
		return true;
	}
 
	public String toString() {
		return String.format("%s | %s | %s | %s | %s | %s | %s" , apiName, apiDesc, version, type, usage, status, fileName);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return apiName.compareTo(((ApiDef) o).apiName);
	}
}
