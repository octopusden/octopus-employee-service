package org.octopusden.employee.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

/**
 * Comprehensive validation tests for OpenShift templates and configuration files.
 * These tests verify the recent refactoring from 'ft' to 'dev' profile naming.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Configuration and Template Validation Tests")
class ConfigurationValidationTest {

    private val yamlMapper = ObjectMapper(YAMLFactory())
    private val projectRoot = File(System.getProperty("user.dir")).parentFile
    
    @Test
    @DisplayName("Validate api-gateway.yaml template structure and content")
    fun testApiGatewayYamlStructure() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        assertTrue(yamlFile.exists(), "api-gateway.yaml should exist")
        
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        // Verify template metadata
        assertEquals("Template", yaml["kind"])
        assertEquals("template.openshift.io/v1", yaml["apiVersion"])
        
        val metadata = yaml["metadata"] as Map<*, *>
        assertEquals("api-gateway-template", metadata["name"])
        
        // Verify objects are present
        val objects = yaml["objects"] as List<*>
        assertTrue(objects.isNotEmpty(), "Template should contain objects")
        
        // Verify ConfigMap with dev naming
        val configMap = objects.find { 
            (it as Map<*, *>)["kind"] == "ConfigMap" 
        } as Map<*, *>
        
        val configMapData = configMap["data"] as Map<*, *>
        assertTrue(configMapData.containsKey("application-dev.yaml"), 
            "ConfigMap should contain application-dev.yaml key")
        
        // Verify no references to old 'ft' naming in data keys
        assertFalse(configMapData.containsKey("application-ft.yaml"),
            "ConfigMap should not contain old application-ft.yaml key")
    }
    
    @Test
    @DisplayName("Validate api-gateway.yaml uses dev profile consistently")
    fun testApiGatewayDevProfile() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        val content = yamlFile.readText()
        
        // Check for dev profile in environment variables
        assertTrue(content.contains("value: \"dev\""), 
            "Should use dev profile")
        
        // Check for dev in mount paths
        assertTrue(content.contains("mountPath: /application-dev.yaml"),
            "Should mount application-dev.yaml")
        assertTrue(content.contains("subPath: application-dev.yaml"),
            "Should reference application-dev.yaml as subPath")
        
        // Check volume naming
        assertTrue(content.contains("name: application-dev"),
            "Volume should be named application-dev")
        
        // Verify no leftover 'ft' references (excluding 'ft' in other contexts)
        val lines = content.lines()
        lines.filter { 
            it.contains("application-ft") || 
            (it.contains("value: \"ft\"") && it.contains("SPRING_PROFILES_ACTIVE"))
        }.forEach { line ->
            fail("Found old 'ft' profile reference: $line")
        }
    }
    
    @Test
    @DisplayName("Validate employee-service.yaml template structure and content")
    fun testEmployeeServiceYamlStructure() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        assertTrue(yamlFile.exists(), "employee-service.yaml should exist")
        
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        // Verify template metadata
        assertEquals("Template", yaml["kind"])
        assertEquals("template.openshift.io/v1", yaml["apiVersion"])
        
        val metadata = yaml["metadata"] as Map<*, *>
        assertEquals("employee-service-template", metadata["name"])
        
        // Verify objects
        val objects = yaml["objects"] as List<*>
        assertTrue(objects.isNotEmpty(), "Template should contain objects")
        
        // Verify ConfigMap with dev naming
        val configMap = objects.find { 
            (it as Map<*, *>)["kind"] == "ConfigMap" 
        } as Map<*, *>
        
        val configMapData = configMap["data"] as Map<*, *>
        assertTrue(configMapData.containsKey("application-dev.yaml"),
            "ConfigMap should contain application-dev.yaml key")
        
        // Verify no old naming
        assertFalse(configMapData.containsKey("application-ft.yaml"),
            "ConfigMap should not contain old application-ft.yaml key")
    }
    
    @Test
    @DisplayName("Validate employee-service.yaml uses dev profile consistently")
    fun testEmployeeServiceDevProfile() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        val content = yamlFile.readText()
        
        // Check for dev profile
        assertTrue(content.contains("value: \"dev\""),
            "Should use dev profile")
        
        // Check mount paths
        assertTrue(content.contains("mountPath: /application-dev.yaml"),
            "Should mount application-dev.yaml")
        assertTrue(content.contains("subPath: application-dev.yaml"),
            "Should reference application-dev.yaml as subPath")
        
        // Check volume naming
        assertTrue(content.contains("name: application-dev"),
            "Volume should be named application-dev")
        
        // Verify no leftover 'ft' references
        val lines = content.lines()
        lines.filter { 
            it.contains("application-ft") || 
            (it.contains("value: \"ft\"") && it.contains("SPRING_PROFILES_ACTIVE"))
        }.forEach { line ->
            fail("Found old 'ft' profile reference: $line")
        }
    }
    
    @Test
    @DisplayName("Validate api-gateway.yaml template parameters")
    fun testApiGatewayTemplateParameters() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val parameters = yaml["parameters"] as List<Map<*, *>>
        
        // Required parameters
        val requiredParams = listOf(
            "DEPLOYMENT_PREFIX",
            "ACTIVE_DEADLINE_SECONDS",
            "API_GATEWAY_VERSION",
            "OCTOPUS_GITHUB_DOCKER_REGISTRY",
            "APPLICATION_DEV_CONTENT",
            "AUTH_SERVER_URL",
            "AUTH_SERVER_REALM",
            "AUTH_SERVER_CLIENT_ID",
            "AUTH_SERVER_CLIENT_SECRET",
            "TEST_EMPLOYEE_SERVICE_HOST",
            "TEST_API_GATEWAY_HOST_EXTERNAL"
        )
        
        val paramNames = parameters.map { it["name"] as String }
        
        requiredParams.forEach { param ->
            assertTrue(paramNames.contains(param),
                "Template should define parameter: $param")
        }
        
        // Verify new APPLICATION_DEV_CONTENT parameter
        val devContentParam = parameters.find { it["name"] == "APPLICATION_DEV_CONTENT" }
        assertNotNull(devContentParam, "APPLICATION_DEV_CONTENT parameter should exist")
        assertEquals(true, devContentParam!!["required"],
            "APPLICATION_DEV_CONTENT should be required")
        assertEquals("application-dev.yaml content", devContentParam["description"])
        
        // Ensure old APPLICATION_FT_CONTENT is not present
        assertFalse(paramNames.contains("APPLICATION_FT_CONTENT"),
            "Old APPLICATION_FT_CONTENT parameter should not exist")
    }
    
    @Test
    @DisplayName("Validate employee-service.yaml template parameters")
    fun testEmployeeServiceTemplateParameters() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val parameters = yaml["parameters"] as List<Map<*, *>>
        
        // Required parameters
        val requiredParams = listOf(
            "DEPLOYMENT_PREFIX",
            "ACTIVE_DEADLINE_SECONDS",
            "EMPLOYEE_SERVICE_VERSION",
            "OCTOPUS_GITHUB_DOCKER_REGISTRY",
            "APPLICATION_DEV_CONTENT",
            "AUTH_SERVER_URL",
            "AUTH_SERVER_REALM",
            "TEST_API_GATEWAY_HOST",
            "TEST_MOCK_SERVER_HOST"
        )
        
        val paramNames = parameters.map { it["name"] as String }
        
        requiredParams.forEach { param ->
            assertTrue(paramNames.contains(param),
                "Template should define parameter: $param")
        }
        
        // Verify new APPLICATION_DEV_CONTENT parameter
        val devContentParam = parameters.find { it["name"] == "APPLICATION_DEV_CONTENT" }
        assertNotNull(devContentParam, "APPLICATION_DEV_CONTENT parameter should exist")
        assertEquals(true, devContentParam!!["required"],
            "APPLICATION_DEV_CONTENT should be required")
        
        // Ensure old parameter is gone
        assertFalse(paramNames.contains("APPLICATION_FT_CONTENT"),
            "Old APPLICATION_FT_CONTENT parameter should not exist")
    }
    
    @Test
    @DisplayName("Validate build.gradle.kts uses APPLICATION_DEV_CONTENT parameter")
    fun testBuildGradleUsesDevContentParameter() {
        val buildFile = File(projectRoot, "ft/build.gradle.kts")
        assertTrue(buildFile.exists(), "ft/build.gradle.kts should exist")
        
        val content = buildFile.readText()
        
        // Verify APPLICATION_DEV_CONTENT is used
        assertTrue(content.contains("\"APPLICATION_DEV_CONTENT\""),
            "build.gradle.kts should reference APPLICATION_DEV_CONTENT parameter")
        
        // Count occurrences (should be 2: gateway and employee)
        val occurrences = "APPLICATION_DEV_CONTENT".toRegex().findAll(content).count()
        assertEquals(2, occurrences,
            "APPLICATION_DEV_CONTENT should appear twice (gateway and employee services)")
        
        // Ensure old parameter name is not used
        assertFalse(content.contains("APPLICATION_FT_CONTENT"),
            "build.gradle.kts should not contain old APPLICATION_FT_CONTENT parameter")
    }
    
    @ParameterizedTest
    @ValueSource(strings = ["okd/api-gateway.yaml", "okd/employee-service.yaml"])
    @DisplayName("Validate YAML files are valid and parseable")
    fun testYamlFilesAreParseable(filePath: String) {
        val yamlFile = File(projectRoot, filePath)
        assertTrue(yamlFile.exists(), "$filePath should exist")
        
        val content = yamlFile.readText()
        
        // Should parse without exceptions
        assertDoesNotThrow({
            yamlMapper.readValue<Map<String, Any>>(content)
        }, "YAML file should be valid: $filePath")
    }
    
    @ParameterizedTest
    @ValueSource(strings = ["okd/api-gateway.yaml", "okd/employee-service.yaml"])
    @DisplayName("Validate OpenShift template has required top-level fields")
    fun testTemplateRequiredFields(filePath: String) {
        val yamlFile = File(projectRoot, filePath)
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        // Required OpenShift template fields
        assertNotNull(yaml["apiVersion"], "$filePath should have apiVersion")
        assertNotNull(yaml["kind"], "$filePath should have kind")
        assertNotNull(yaml["metadata"], "$filePath should have metadata")
        assertNotNull(yaml["objects"], "$filePath should have objects")
        assertNotNull(yaml["parameters"], "$filePath should have parameters")
        
        // Verify objects is a non-empty list
        val objects = yaml["objects"] as List<*>
        assertTrue(objects.isNotEmpty(), "$filePath should have at least one object")
    }
    
    @Test
    @DisplayName("Validate api-gateway template contains all required Kubernetes resources")
    fun testApiGatewayRequiredResources() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val kinds = objects.map { it["kind"] as String }
        
        // Required resource types
        assertTrue(kinds.contains("ConfigMap"), "Should have ConfigMap")
        assertTrue(kinds.contains("Pod"), "Should have Pod")
        assertTrue(kinds.contains("Service"), "Should have Service")
        assertTrue(kinds.contains("Route"), "Should have Route")
    }
    
    @Test
    @DisplayName("Validate employee-service template contains all required Kubernetes resources")
    fun testEmployeeServiceRequiredResources() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val kinds = objects.map { it["kind"] as String }
        
        // Required resource types
        assertTrue(kinds.contains("ConfigMap"), "Should have ConfigMap")
        assertTrue(kinds.contains("Pod"), "Should have Pod")
        assertTrue(kinds.contains("Service"), "Should have Service")
        assertTrue(kinds.contains("Route"), "Should have Route")
    }
    
    @Test
    @DisplayName("Validate Pod specifications in api-gateway.yaml")
    fun testApiGatewayPodSpec() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
        
        assertNotNull(pod, "Pod resource should exist")
        
        val spec = pod["spec"] as Map<*, *>
        assertEquals("Never", spec["restartPolicy"], "Pod should have Never restart policy")
        assertTrue(spec.containsKey("activeDeadlineSeconds"), 
            "Pod should have activeDeadlineSeconds")
        
        val containers = spec["containers"] as List<Map<*, *>>
        assertEquals(1, containers.size, "Should have exactly one container")
        
        val container = containers[0]
        assertEquals("gateway", container["name"])
        
        // Verify environment variables
        val env = container["env"] as List<Map<*, *>>
        val envNames = env.map { it["name"] as String }
        
        assertTrue(envNames.contains("SPRING_PROFILES_ACTIVE"),
            "Should have SPRING_PROFILES_ACTIVE env var")
        
        val profileEnv = env.find { it["name"] == "SPRING_PROFILES_ACTIVE" }
        assertEquals("dev", (profileEnv!!["value"] as String).replace("\${", "").replace("}", ""),
            "SPRING_PROFILES_ACTIVE should reference dev")
    }
    
    @Test
    @DisplayName("Validate Pod specifications in employee-service.yaml")
    fun testEmployeeServicePodSpec() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
        
        assertNotNull(pod, "Pod resource should exist")
        
        val spec = pod["spec"] as Map<*, *>
        assertEquals("Never", spec["restartPolicy"])
        
        val containers = spec["containers"] as List<Map<*, *>>
        val container = containers[0]
        assertEquals("employee", container["name"])
        
        // Verify environment variables
        val env = container["env"] as List<Map<*, *>>
        val profileEnv = env.find { it["name"] == "SPRING_PROFILES_ACTIVE" }
        assertNotNull(profileEnv, "Should have SPRING_PROFILES_ACTIVE env var")
    }
    
    @Test
    @DisplayName("Validate volume mounts use consistent naming in api-gateway")
    fun testApiGatewayVolumeMountConsistency() {
        val yamlFile = File(projectRoot, "okd/api-gateway.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
        val spec = pod["spec"] as Map<*, *>
        
        // Check container volume mounts
        val containers = spec["containers"] as List<Map<*, *>>
        val volumeMounts = containers[0]["volumeMounts"] as List<Map<*, *>>
        
        assertEquals(1, volumeMounts.size, "Should have one volume mount")
        val mount = volumeMounts[0]
        assertEquals("application-dev", mount["name"],
            "Volume mount name should be application-dev")
        assertEquals("/application-dev.yaml", mount["mountPath"],
            "Mount path should be /application-dev.yaml")
        assertEquals("application-dev.yaml", mount["subPath"],
            "SubPath should be application-dev.yaml")
        
        // Check volumes
        val volumes = spec["volumes"] as List<Map<*, *>>
        assertEquals(1, volumes.size, "Should have one volume")
        val volume = volumes[0]
        assertEquals("application-dev", volume["name"],
            "Volume name should be application-dev")
    }
    
    @Test
    @DisplayName("Validate volume mounts use consistent naming in employee-service")
    fun testEmployeeServiceVolumeMountConsistency() {
        val yamlFile = File(projectRoot, "okd/employee-service.yaml")
        val content = yamlFile.readText()
        val yaml = yamlMapper.readValue<Map<String, Any>>(content)
        
        val objects = yaml["objects"] as List<Map<*, *>>
        val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
        val spec = pod["spec"] as Map<*, *>
        
        // Check container volume mounts
        val containers = spec["containers"] as List<Map<*, *>>
        val volumeMounts = containers[0]["volumeMounts"] as List<Map<*, *>>
        
        val mount = volumeMounts[0]
        assertEquals("application-dev", mount["name"])
        assertEquals("/application-dev.yaml", mount["mountPath"])
        assertEquals("application-dev.yaml", mount["subPath"])
        
        // Check volumes
        val volumes = spec["volumes"] as List<Map<*, *>>
        val volume = volumes[0]
        assertEquals("application-dev", volume["name"])
    }
    
    @Test
    @DisplayName("Validate no legacy 'ft' references exist in configuration files")
    fun testNoLegacyFtReferences() {
        val filesToCheck = listOf(
            "ft/build.gradle.kts",
            "okd/api-gateway.yaml",
            "okd/employee-service.yaml"
        )
        
        filesToCheck.forEach { filePath ->
            val file = File(projectRoot, filePath)
            val content = file.readText()
            
            // Check for problematic patterns
            val problematicPatterns = listOf(
                "APPLICATION_FT_CONTENT",
                "application-ft.yaml",
                "name: application-ft",
                "mountPath: /application-ft.yaml",
                "subPath: application-ft.yaml",
                "key: application-ft.yaml",
                "path: application-ft.yaml"
            )
            
            problematicPatterns.forEach { pattern ->
                assertFalse(content.contains(pattern),
                    "$filePath should not contain legacy pattern: $pattern")
            }
        }
    }
    
    @Test
    @DisplayName("Validate ConfigMap references in both templates")
    fun testConfigMapReferences() {
        val templates = mapOf(
            "okd/api-gateway.yaml" to "gateway-config-data",
            "okd/employee-service.yaml" to "employee-config-data"
        )
        
        templates.forEach { (filePath, configSuffix) ->
            val yamlFile = File(projectRoot, filePath)
            val content = yamlFile.readText()
            val yaml = yamlMapper.readValue<Map<String, Any>>(content)
            
            val objects = yaml["objects"] as List<Map<*, *>>
            
            // Find ConfigMap
            val configMap = objects.find { it["kind"] == "ConfigMap" } as Map<*, *>
            val configMapMetadata = configMap["metadata"] as Map<*, *>
            assertTrue((configMapMetadata["name"] as String).contains(configSuffix),
                "$filePath ConfigMap name should contain $configSuffix")
            
            // Find Pod and verify it references the ConfigMap
            val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
            val podSpec = pod["spec"] as Map<*, *>
            val volumes = podSpec["volumes"] as List<Map<*, *>>
            val volume = volumes[0]
            val configMapRef = volume["configMap"] as Map<*, *>
            
            assertTrue((configMapRef["name"] as String).contains(configSuffix),
                "$filePath Pod should reference ConfigMap with $configSuffix")
        }
    }
    
    @Test
    @DisplayName("Validate readiness probes are configured")
    fun testReadinessProbes() {
        val templates = listOf("okd/api-gateway.yaml", "okd/employee-service.yaml")
        
        templates.forEach { filePath ->
            val yamlFile = File(projectRoot, filePath)
            val content = yamlFile.readText()
            val yaml = yamlMapper.readValue<Map<String, Any>>(content)
            
            val objects = yaml["objects"] as List<Map<*, *>>
            val pod = objects.find { it["kind"] == "Pod" } as Map<*, *>
            val spec = pod["spec"] as Map<*, *>
            val containers = spec["containers"] as List<Map<*, *>>
            val container = containers[0]
            
            assertTrue(container.containsKey("readinessProbe"),
                "$filePath should have readiness probe")
            
            val probe = container["readinessProbe"] as Map<*, *>
            val httpGet = probe["httpGet"] as Map<*, *>
            assertEquals("/actuator/health/readiness", httpGet["path"],
                "Readiness probe should check actuator health endpoint")
        }
    }
    
    @Test
    @DisplayName("Validate Service and Route configurations")
    fun testServiceAndRoute() {
        val configs = mapOf(
            "okd/api-gateway.yaml" to 8765,
            "okd/employee-service.yaml" to 8080
        )
        
        configs.forEach { (filePath, expectedPort) ->
            val yamlFile = File(projectRoot, filePath)
            val content = yamlFile.readText()
            val yaml = yamlMapper.readValue<Map<String, Any>>(content)
            
            val objects = yaml["objects"] as List<Map<*, *>>
            
            // Validate Service
            val service = objects.find { it["kind"] == "Service" } as Map<*, *>
            assertNotNull(service, "$filePath should have Service")
            
            val serviceSpec = service["spec"] as Map<*, *>
            val ports = serviceSpec["ports"] as List<Map<*, *>>
            assertEquals(expectedPort, ports[0]["port"],
                "$filePath Service should expose port $expectedPort")
            
            // Validate Route
            val route = objects.find { it["kind"] == "Route" } as Map<*, *>
            assertNotNull(route, "$filePath should have Route")
            
            val routeSpec = route["spec"] as Map<*, *>
            val routePort = routeSpec["port"] as Map<*, *>
            assertEquals(expectedPort, routePort["targetPort"],
                "$filePath Route should target port $expectedPort")
        }
    }
    
    @Test
    @DisplayName("Validate all parameters are marked as required where appropriate")
    fun testRequiredParameters() {
        val templates = listOf("okd/api-gateway.yaml", "okd/employee-service.yaml")
        
        templates.forEach { filePath ->
            val yamlFile = File(projectRoot, filePath)
            val content = yamlFile.readText()
            val yaml = yamlMapper.readValue<Map<String, Any>>(content)
            
            val parameters = yaml["parameters"] as List<Map<*, *>>
            
            // All parameters in these templates should be required
            parameters.forEach { param ->
                assertEquals(true, param["required"],
                    "Parameter ${param["name"]} in $filePath should be required")
            }
        }
    }
}