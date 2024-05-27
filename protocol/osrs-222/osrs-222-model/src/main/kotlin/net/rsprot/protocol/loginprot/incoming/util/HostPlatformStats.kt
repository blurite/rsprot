package net.rsprot.protocol.loginprot.incoming.util

@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class HostPlatformStats(
    private val _version: UByte,
    private val _osType: UByte,
    public val os64Bit: Boolean,
    private val _osVersion: UShort,
    private val _javaVendor: UByte,
    private val _javaVersionMajor: UByte,
    private val _javaVersionMinor: UByte,
    private val _javaVersionPatch: UByte,
    public val applet: Boolean,
    private val _javaMaxMemoryMb: UShort,
    private val _javaAvailableProcessors: UByte,
    public val systemMemory: Int,
    private val _systemSpeed: UShort,
    public val gpuDxName: String,
    public val gpuGlName: String,
    public val gpuDxVersion: String,
    public val gpuGlVersion: String,
    private val _gpuDriverMonth: UByte,
    private val _gpuDriverYear: UShort,
    public val cpuManufacturer: String,
    public val cpuBrand: String,
    private val _cpuCount1: UByte,
    private val _cpuCount2: UByte,
    public val cpuFeatures: IntArray,
    public val cpuSignature: Int,
    public val clientName: String,
    public val deviceName: String,
) {
    public val version: Int
        get() = _version.toInt()
    public val osType: Int
        get() = _osType.toInt()
    public val osVersion: Int
        get() = _osVersion.toInt()
    public val javaVendor: Int
        get() = _javaVendor.toInt()
    public val javaVersionMajor: Int
        get() = _javaVersionMajor.toInt()
    public val javaVersionMinor: Int
        get() = _javaVersionMinor.toInt()
    public val javaVersionPatch: Int
        get() = _javaVersionPatch.toInt()
    public val javaMaxMemoryMb: Int
        get() = _javaMaxMemoryMb.toInt()
    public val javaAvailableProcessors: Int
        get() = _javaAvailableProcessors.toInt()
    public val systemSpeed: Int
        get() = _systemSpeed.toInt()
    public val gpuDriverMonth: Int
        get() = _gpuDriverMonth.toInt()
    public val gpuDriverYear: Int
        get() = _gpuDriverYear.toInt()
    public val cpuCount1: Int
        get() = _cpuCount1.toInt()
    public val cpuCount2: Int
        get() = _cpuCount2.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HostPlatformStats

        if (_version != other._version) return false
        if (_osType != other._osType) return false
        if (os64Bit != other.os64Bit) return false
        if (_osVersion != other._osVersion) return false
        if (_javaVendor != other._javaVendor) return false
        if (_javaVersionMajor != other._javaVersionMajor) return false
        if (_javaVersionMinor != other._javaVersionMinor) return false
        if (_javaVersionPatch != other._javaVersionPatch) return false
        if (applet != other.applet) return false
        if (_javaMaxMemoryMb != other._javaMaxMemoryMb) return false
        if (_javaAvailableProcessors != other._javaAvailableProcessors) return false
        if (systemMemory != other.systemMemory) return false
        if (_systemSpeed != other._systemSpeed) return false
        if (gpuDxName != other.gpuDxName) return false
        if (gpuGlName != other.gpuGlName) return false
        if (gpuDxVersion != other.gpuDxVersion) return false
        if (gpuGlVersion != other.gpuGlVersion) return false
        if (_gpuDriverMonth != other._gpuDriverMonth) return false
        if (_gpuDriverYear != other._gpuDriverYear) return false
        if (cpuManufacturer != other.cpuManufacturer) return false
        if (cpuBrand != other.cpuBrand) return false
        if (_cpuCount1 != other._cpuCount1) return false
        if (_cpuCount2 != other._cpuCount2) return false
        if (!cpuFeatures.contentEquals(other.cpuFeatures)) return false
        if (cpuSignature != other.cpuSignature) return false
        if (clientName != other.clientName) return false
        if (deviceName != other.deviceName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _version.hashCode()
        result = 31 * result + _osType.hashCode()
        result = 31 * result + os64Bit.hashCode()
        result = 31 * result + _osVersion.hashCode()
        result = 31 * result + _javaVendor.hashCode()
        result = 31 * result + _javaVersionMajor.hashCode()
        result = 31 * result + _javaVersionMinor.hashCode()
        result = 31 * result + _javaVersionPatch.hashCode()
        result = 31 * result + applet.hashCode()
        result = 31 * result + _javaMaxMemoryMb.hashCode()
        result = 31 * result + _javaAvailableProcessors.hashCode()
        result = 31 * result + systemMemory
        result = 31 * result + _systemSpeed.hashCode()
        result = 31 * result + gpuDxName.hashCode()
        result = 31 * result + gpuGlName.hashCode()
        result = 31 * result + gpuDxVersion.hashCode()
        result = 31 * result + gpuGlVersion.hashCode()
        result = 31 * result + _gpuDriverMonth.hashCode()
        result = 31 * result + _gpuDriverYear.hashCode()
        result = 31 * result + cpuManufacturer.hashCode()
        result = 31 * result + cpuBrand.hashCode()
        result = 31 * result + _cpuCount1.hashCode()
        result = 31 * result + _cpuCount2.hashCode()
        result = 31 * result + cpuFeatures.contentHashCode()
        result = 31 * result + cpuSignature
        result = 31 * result + clientName.hashCode()
        result = 31 * result + deviceName.hashCode()
        return result
    }

    override fun toString(): String {
        return "HostPlatformStats(" +
            "os64Bit=$os64Bit, " +
            "systemMemory=$systemMemory, " +
            "gpuDxName='$gpuDxName', " +
            "gpuGlName='$gpuGlName', " +
            "gpuDxVersion='$gpuDxVersion', " +
            "gpuGlVersion='$gpuGlVersion', " +
            "cpuManufacturer='$cpuManufacturer', " +
            "cpuBrand='$cpuBrand', " +
            "cpuFeatures=${cpuFeatures.contentToString()}, " +
            "cpuSignature=$cpuSignature, " +
            "clientName='$clientName', " +
            "deviceName='$deviceName', " +
            "version=$version, " +
            "osType=$osType, " +
            "osVersion=$osVersion, " +
            "javaVendor=$javaVendor, " +
            "javaVersionMajor=$javaVersionMajor, " +
            "javaVersionMinor=$javaVersionMinor, " +
            "javaVersionPatch=$javaVersionPatch, " +
            "applet=$applet, " +
            "javaMaxMemoryMb=$javaMaxMemoryMb, " +
            "javaAvailableProcessors=$javaAvailableProcessors, " +
            "systemSpeed=$systemSpeed, " +
            "gpuDriverMonth=$gpuDriverMonth, " +
            "gpuDriverYear=$gpuDriverYear, " +
            "cpuCount1=$cpuCount1, " +
            "cpuCount2=$cpuCount2" +
            ")"
    }
}
