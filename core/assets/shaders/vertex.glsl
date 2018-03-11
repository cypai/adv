attribute vec4 a_position;
attribute vec4 a_color;
attribute vec4 a_color_inter1;
attribute vec4 a_color_inter2;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec4 v_color_inter1;
varying vec4 v_color_inter2;
varying vec2 v_texCoords;

void main() {
    v_color = a_color;
    v_color_inter1 = a_color_inter1;
    v_color_inter2 = a_color_inter2;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
