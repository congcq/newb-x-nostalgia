$input v_color0, v_pos

#include <bgfx_shader.sh>
#include <newb/main.sh>

void main() {
  vec4 diffuse = v_color0;
  diffuse.rgb += vec3(0.12, 0.12, 0.17) + vec3(-0.1,-0.1,-0.05)*cos(2.0*diffuse.rgb);
  diffuse.rgb = colorCorrection(diffuse.rgb);
  diffuse.a *= 0.1 + 1.0*cos(2.0*diffuse.a);
  gl_FragColor = diffuse;
}

