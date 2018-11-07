#version 430

in vec2 tc;// added
in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
out vec4 fragColor;
 
struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};

struct Material
{	vec4 ambient, diffuse, specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix; 
uniform mat4 proj_matrix;
uniform mat4 normalMat;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D texSamp; // added

void main(void)
{	vec3 L = normalize(vLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(vHalfVec);
	vec4 texColor = texture(texSamp, tc);
	vec4 lightColor = (light.ambient * material.ambient) + (light.diffuse * material.diffuse) + light.specular;
	
	
	float inShadow = textureProj(shadowTex, shadow_coord);
	
	fragColor = globalAmbient * material.ambient
				+ light.ambient * material.ambient;
	
	//fragColor = 0.05 * texColor + 0.05 * lightColor;
	
	if (inShadow != 0.0)
	{
		//fragColor = texColor * (globalAmbient + light.ambient + light.diffuse * max(dot(L, N), 0.0))
				+ light.specular * pow(max(dot(H,N), 0.0), material.shininess * 3.0);
		
		fragColor += light.diffuse * material.diffuse * max(dot(L,N),0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0);
	
		
	}
}
