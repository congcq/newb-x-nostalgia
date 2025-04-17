$input v_color0, v_color1, v_fog, v_refl, v_texcoord0, v_lightmapUV, v_extra, v_position, v_worldPos

#include <bgfx_shader.sh>
#include <newb/main.sh>

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

void main() {
  #if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY) || defined(INSTANCING)
    gl_FragColor = vec4(1.0,1.0,1.0,1.0);
    return;
  #endif

  vec3 viewDir = normalize(v_worldPos);
  viewDir.y = -viewDir.y;
vec2 uvl = v_lightmapUV;

nl_environment env = nlDetectEnvironment(FogColor.rgb, FogAndDistanceControl.xyz);
nl_skycolor skycol = nlSkyColors(env, FogColor.rgb);

float rain = detectRain(FogAndDistanceControl.xyz);
float time = ViewPositionAndTime.w;
  
  
  float day = pow(max(min(1.0 - FogColor.r * 1.2, 1.0), 0.0), 0.4);
  float night = pow(max(min(1.0 - FogColor.r * 1.5, 1.0), 0.0), 1.2);
  float dusk = max(FogColor.r - FogColor.b, 0.0);
  float cave = smoothstep(0.5,0.0, uvl.y); 
  

  vec4 diffuse = texture2D(s_MatTexture, v_texcoord0);
  vec4 color = v_color0;

float shadowmap = smoothstep(0.875, 0.81, pow(uvl.y,2.0));
  shadowmap *= mix(1.0, 0.0, env.rainFactor);
  shadowmap *= mix(1.0,0.0,pow(uvl.x * 1.2, 7.0));
  shadowmap *= mix(1.0, 0.5, night);
  diffuse.rgb *= 1.0-0.3*shadowmap;
  
vec3 normal = normalize(cross(dFdx(v_position),dFdy(v_position)));
float dirfac = 0.25;

  dirfac = mix(mix(dirfac, 0.0, smoothstep(0.875, 0.81, pow(uvl.y,2.0))),0.0, pow(uvl.x * 1.2, 7.0));
if (!env.underwater) {
  dirfac = mix(dirfac, 0.0, env.rainFactor);
}

if (!env.nether && !env.end) {
#if NL_CLOUD_TYPE != 0
diffuse.rgb *= 1.0-dirfac*abs(normal.x);
#endif
}

  
vec3 torchColor;
   if (env.underwater) {
    torchColor = NL_UNDERWATER_TORCH_COL;
  } else if (env.end) {
    torchColor = NL_END_TORCH_COL;
  } else if (env.nether) {
    torchColor = NL_NETHER_TORCH_COL;
  } else {
    torchColor = NL_OVERWORLD_TORCH_COL;
  }

    
  #ifdef ALPHA_TEST
    if (diffuse.a < 0.6) {
      discard;
    }
  #endif

  #if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *= mix(vec3(1.0,1.0,1.0), texture2D(s_SeasonsTexture, v_color1.xy).rgb * 2.0, v_color1.z);
  #endif

  vec3 glow = nlGlow(s_MatTexture, v_texcoord0, v_extra.a);

  diffuse.rgb *= diffuse.rgb;

  vec3 lightTint = texture2D(s_LightMapTexture, v_lightmapUV).rgb;
  lightTint = mix(lightTint.bbb, lightTint*lightTint, 0.35 + 0.65*v_lightmapUV.y*v_lightmapUV.y*v_lightmapUV.y);

  color.rgb *= lightTint;

  #if defined(TRANSPARENT) && !(defined(SEASONS) || defined(RENDER_AS_BILLBOARDS))
    if (v_extra.b > 0.9) {
      diffuse.rgb = vec3_splat(1.0 - NL_WATER_TEX_OPACITY*(1.0 - diffuse.b*1.8));
      diffuse.a = color.a;
    }
  #else
    diffuse.a = 1.0;
  #endif

  diffuse.rgb *= color.rgb;
  diffuse.rgb += glow;

vec3 skycolor = nlRenderSky(skycol, env, viewDir, FogColor.rgb, ViewPositionAndTime.w);

    float specular = smoothstep(0.15, 0.0, abs(viewDir.z));
    specular *= specular*smoothstep(0.75, 1.0,abs(viewDir.x));
    specular *= specular;
    specular += specular*specular*specular*specular;
    specular *= max(FogColor.r-FogColor.b, 0.0);
    vec3 sunrefl = 2.5*(NL_DAWN_EDGE_COL+skycol.horizonEdge) * specular * specular;
    sunrefl += sunrefl;
    
  if (v_extra.b > 0.9) {
    
    diffuse.rgb += v_refl.rgb*v_refl.a;
    diffuse.rgb += sunrefl*v_refl.a;
    diffuse.rgb += torchColor*pow(uvl.x * 1.2, 7.0);
  } else if (v_refl.a > 0.0) {
    // reflective effect - only on xz plane
    float dy = abs(dFdy(v_extra.g));
    if (dy < 0.0002) {
      float mask = v_refl.a*(clamp(v_extra.r*10.0,8.2,8.8)-7.8);
      diffuse.rgb *= 1.0 - 0.6*mask;
      diffuse.rgb += v_refl.rgb*mask;
    }
  }
  
    #undef NL_UNDERWATER_TINT
    #define NL_UNDERWATER_TINT skycolor
    #undef NL_WATER_TINT
    #define NL_WATER_TINT skycolor

  diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);

  diffuse.rgb = colorCorrection(diffuse.rgb);

  gl_FragColor = diffuse;
}
