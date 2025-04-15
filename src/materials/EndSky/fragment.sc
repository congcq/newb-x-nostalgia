#ifndef INSTANCING
$input v_texcoord0, v_posTime, v_position
#endif

#include <bgfx_shader.sh>

#ifndef INSTANCING
  #include <newb/main.sh>
vec3 renderSunf(vec3 sPos, float sunAngle, float height, float scale, float sunBrightness){
vec3 color = vec3(0.0,0.0,0.0);

float angle = sunAngle*2.3172533;
      float sinA = sin(angle);
      float cosA = cos(angle);
      sPos.yz = vec2(sPos.y*cosA - sPos.z*sinA, sPos.y*sinA + sPos.z*cosA);

sPos.y *= height;

vec3 sunDir = normalize(sPos);
float sunShape = smoothstep(0.9,1.0,sunDir.z*0.91*scale);


vec3 sunCol = NL_END_HORIZON_COL*100000.0*sunShape;
sunCol += sunCol;
sunCol *= smoothstep(0.0, 0.1, sunShape);



color.rgb += sunCol;



return color.rgb*sunBrightness;
}


  SAMPLER2D_AUTOREG(s_SkyTexture);
#endif

vec3 hash(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 740.7)), dot(p, vec3(29.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0* f);
    return mix(mix(mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 0.0, 0.0)), dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(1.0, 0.0, 0.0)), u.x), mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 1.0, 0.0)), dot(hash(i + vec3(0.0,0.0, 0.0)), f - vec3(1.0, 1.0, 0.0)), u.x), u.y), mix(mix(dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(0.0, 0.0, 1.0)), dot(hash(i + vec3(0.0, 0.0, 0.0)), f - vec3(1.0, 0.0, 1.0)), u.x), mix(dot(hash(i + vec3(0.0, 1.0, 1.0)), f - vec3(0.0, 1.0, 1.0)), dot(hash(i + vec3(0.0, 1.0, 1.0)), f - vec3(1.0, 1.0, 1.0)), u.x), u.y), u.z);
}

void main() {
  #ifndef INSTANCING
    vec4 diffuse = texture2D(s_SkyTexture, v_texcoord0);

    vec3 color = renderEndSky(getEndHorizonCol(), getEndZenithCol(), normalize(v_posTime.xyz), v_posTime.w);
    color += 2.8*diffuse.rgb; // stars
    
    vec3 stars_direction = normalize(v_position.xyz);
    float stars_threshold = 10.5;
    float stars_exposure = 500.0;
    float stars = pow(clamp(noise(stars_direction * 300.0), 0.0, 1.0), stars_threshold) * stars_exposure;
    stars *= mix(0.4, 1.4, noise(stars_direction * 900.0));
    color.rgb += vec3(stars,stars,stars)*vec3(5.0, 3.0,5.0);//màu của sao
    color.rgb += renderSunf(v_posTime.zyx,445.0, 1.0, 0.997, 0.4)*0.5;
    color = colorCorrection(color);
    
    gl_FragColor = vec4(color, 1.0);
  #else
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
  #endif
}
