package se.sogeti.app.models.dto;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LinkDTO implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("href")
    @Expose
    private String href;

    @SerializedName("isOpen")
    @Expose
    private boolean isOpen;

    private static final long serialVersionUID = 8904919229245891491L;

    public LinkDTO() {
    }

    public LinkDTO(String href) {
        super();
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkDTO withId(String id) {
        this.id = id;
        return this;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public LinkDTO withHref(String href) {
        this.href = href;
        return this;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public LinkDTO withIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("href", href).append("isOpen", isOpen).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(href).append(isOpen).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LinkDTO)) {
            return false;
        }
        LinkDTO rhs = ((LinkDTO) other);
        return new EqualsBuilder().append(id, rhs.id).append(href, rhs.href).append(isOpen, rhs.isOpen).isEquals();
    }

}