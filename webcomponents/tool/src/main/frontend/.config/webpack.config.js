const CKEditorWebpackPlugin = require('@ckeditor/ckeditor5-dev-webpack-plugin');
const {styles} = require('@ckeditor/ckeditor5-dev-utils');

module.exports = {
    entry: ['./conf/ckeditor-src.js'],
    output: {
        library: 'CKEDITOR',
        filename: "ckeditor.js"
    },
    plugins: [
        new CKEditorWebpackPlugin({
            // See https://ckeditor.com/docs/ckeditor5/latest/features/ui-language.html
            language: 'en',
            additionalLanguages: 'all'
        })
    ],
    module: {
        rules: [
            {
                // Or /ckeditor5-[^/]+\/theme\/icons\/.+\.svg$/ if you want to limit this loader
                // to CKEditor 5 icons only.
                test: /\.svg$/,

                use: ['raw-loader']
            },
            {
                // Or /ckeditor5-[^/]+\/theme\/[\w-/]+\.css$/ if you want to limit this loader
                // to CKEditor 5 theme only.
                test: /\.css$/,
                use: [
                    {
                        loader: 'style-loader',
                        options: {
                            injectType: 'singletonStyleTag'
                        }
                    },
                    {
                        loader: 'postcss-loader',
                        options: styles.getPostCssConfig({
                            themeImporter: {
                                themePath: require.resolve('@ckeditor/ckeditor5-theme-lark')
                            },
                            minify: true
                        })
                    },
                ]
            }
        ],
    }
};
