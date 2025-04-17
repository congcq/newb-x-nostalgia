#ifndef FOG_H
#define FOG_H

#include "detection.h"

float nlRenderFogFade(float relativeDist, vec3 FOG_COLOR, vec2 FOG_CONTROL) {
  #ifdef NL_FOG
  
  float expectedFogX = 0.029 + (0.09*FOG_CONTROL.y*FOG_CONTROL.y);

float night = pow(max(min(1.0 - FOG_COLOR.r * 1.5, 1.0), 0.0), 1.2);

  // nether wastes, basalt delta, crimson forest, wrapped forest, soul sand valley
  bool netherFogCtrl = (FOG_CONTROL.x<0.14  && abs(FOG_CONTROL.x-expectedFogX) < 0.02);
  bool netherFogCol = (FOG_COLOR.r+FOG_COLOR.g)>0.0;
  bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;
bool nether = (netherFogCtrl && netherFogCol) || underLava;
bool end = FOG_COLOR.r==FOG_COLOR.b && (FOG_COLOR.r-FOG_COLOR.g>0.24 || (FOG_COLOR.g==0.0 && FOG_COLOR.r>0.1));
if(end){
relativeDist *= 4.0;
FOG_CONTROL.xy *= smoothstep(0.0, 1.0, relativeDist);
} else if(nether){
relativeDist *= 2.0;
FOG_CONTROL.xy *= smoothstep(0.0, 1.0, relativeDist);
}
    float fade = smoothstep(FOG_CONTROL.x, FOG_CONTROL.y, relativeDist);
     
    // misty effect
    
    float density = NL_MIST_DENSITY*(19.0 - 18.0*FOG_COLOR.g);
    density *= mix(1.0, 1.5, night);
    fade += (1.0-fade)*(0.3-0.3*exp(-relativeDist*relativeDist*density));
if(end){
    fade *= 0.4;
} else if(nether){
    fade *= 0.35;
}

    return NL_FOG * fade;
  #else
    return 0.0;
  #endif
}


float nlRenderGodRayIntensity(vec3 cPos, vec3 worldPos, float t, vec2 uv1, float relativeDist, vec3 FOG_COLOR) {
  // offset wPos (only works upto 16 blocks)
  vec3 offset = cPos -30.0*fract(worldPos*0.0);
  offset = abs(2.0*fract(offset*0.0625)-1.0);
  offset = offset*offset*(3.0-2.0*offset);
  //offset = 0.5 + 0.5*cos(offset*0.392699082);

  //vec3 ofPos = wPos+offset;
  vec3 nrmof = normalize(worldPos);

  float u = nrmof.z/length(nrmof.zy);
  float diff = dot(offset,vec3(0.1,0.2,1.0)) + 0.07*t;
  float mask = nrmof.x*nrmof.x;

  float vol = sin(7.0*u + 1.5*diff)*sin(3.0*u + diff);
  vol += sin(2.0*u + 0.3*diff)*sin(3.0*u + 0.2*diff);
  vol *= vol*mask*uv1.y*(1.0-mask*mask);
  vol *= relativeDist*relativeDist;
  vol = clamp(vol,0.0,1.0);
  // dawn/dusk mask
  vol *= clamp(3.0*(FOG_COLOR.r-FOG_COLOR.b), 0.0, 1.0);

  vol = smoothstep(0.0, 0.1, vol);
  return vol;
}

#endif
