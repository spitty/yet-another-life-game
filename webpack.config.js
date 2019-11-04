const webpack = require("webpack");
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require("path");

const dist = path.resolve(__dirname, "build/dist");

module.exports = {
    entry: {
        main: "main"
    },
    output: {
        filename: "[name].bundle.js",
        path: dist,
        publicPath: ""
    },
    devServer: {
        contentBase: dist
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    resolve: {
        modules: [
            path.resolve(__dirname, "build/classes/kotlin/main/min/"),
            path.resolve(__dirname, "src/main/web/")
        ]
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Yet Another Game of Life (Kotlin JS Example)'
        }),
        new webpack.optimize.UglifyJsPlugin()
    ]
};
