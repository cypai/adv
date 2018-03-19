#ifdef GL_ES
#define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying LOWP vec4 v_color;
varying LOWP vec4 v_color_inter1;
varying LOWP vec4 v_color_inter2;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
    if (v_color_inter1.a == 0.0) {
        if (v_color_inter2.a == 0.0) {
            gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
        } else {
            vec4 tex_color = texture2D(u_texture, v_texCoords);
            float gray = (tex_color.r + tex_color.g + tex_color.b) / 3.0;
            if (gray < v_color_inter2.a) {
                vec3 colorized = v_color_inter2.rgb * (gray + 0.2);
                gl_FragColor = vec4(colorized, tex_color.a);
            } else {
                gl_FragColor = tex_color;
            }
        }
    } else {
        if (v_color_inter2.a == 0.0) {
            gl_FragColor = v_color_inter1 * texture2D(u_texture, v_texCoords);
        } else {
            vec4 tex_color = texture2D(u_texture, v_texCoords);
            float gray = (tex_color.r + tex_color.g + tex_color.b) / 3.0;
            float boost = 1.0 - v_color_inter2.a;
            float boosted_gray = gray;
            if (gray < 0.5) {
                boosted_gray = gray - gray * boost;
            } else if (gray > 0.5) {
                boosted_gray = gray + gray * boost;
            }
            vec3 grayscale = vec3(boosted_gray);
            vec3 inverse_grayscale = vec3(1.0 - boosted_gray);
            vec3 color_inter1 = v_color_inter1.rgb * grayscale;
            vec3 color_inter2 = v_color_inter2.rgb * inverse_grayscale;
            vec3 actual_color = color_inter1 + color_inter2;

            gl_FragColor = vec4(actual_color, tex_color.a);
        }
    }
}
