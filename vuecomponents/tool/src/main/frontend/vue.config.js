const { BundleAnalyzerPlugin } = require("webpack-bundle-analyzer");

const shouldGenerateStats = process.env.GENERATE_STATS === "1";

module.exports = {
  configureWebpack: (config) => {
    if (!shouldGenerateStats) {
      return;
    }

    config.plugins = config.plugins || [];
    config.plugins.push(
      new BundleAnalyzerPlugin({
        analyzerMode: "disabled",
        generateStatsFile: true,
        statsFilename: "report.json",
        openAnalyzer: false,
      })
    );
  },
};
