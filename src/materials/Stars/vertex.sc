$input a_color0, a_position
$output v_color0, v_pos

#include <bgfx_shader.sh>

uniform vec4 StarsColor;

void main() {
#ifndef INSTANCING
  vec3 pos = a_position;
  vec3 worldPos = mul(u_model[0], vec4(pos, 1.0)).xyz;

  vec4 color = a_color0;
  //color.rgb *= 0.6 + 0.4*sin(2.0*pos);
  color.rgb *= StarsColor.rgb;
  color.rgb *= vec3(0.9, 0.9, 1.1) + vec3(0.2,0.3,0.4)*sin(2.0*color.rgb);
  color.a *= 0.6 + 0.3*sin(0.5*color.a);
  //color.rgb *= mix(vec3(0.4,0.6,0.7), vec3(0.4, 0.7, 1.2)*1.5, 0.6+0.4*sin(0.5*pos));
  v_color0 = color;
  v_pos = pos;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
