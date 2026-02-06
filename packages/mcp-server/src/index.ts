import { createHTTPServer, Tool, MCPServer, SchemaConstraint } from "@leanmcp/core";
import { spawn } from "child_process";
import path from "path";

// Define Schema using Zod (standard for MCP tools)


class OcrInput {
    @SchemaConstraint({ description: "Absolute path to the image file to process" })
    imagePath!: string;
}

class TextLexiqTools {
    @Tool({
        description: "Extract text from an image document using TextLexiq engine",
        inputClass: OcrInput,
    })
    async ocr_document(input: OcrInput) {
        return new Promise((resolve, reject) => {
            // Resolve path to python script
            // Assuming structure: packages/mcp-server/src/index.ts -> packages/python-logic/main.py
            const scriptPath = path.resolve(__dirname, "../../python-logic/main.py");

            const pythonProcess = spawn("python3", [scriptPath, "ocr", input.imagePath]);

            let output = "";
            let errorM = "";

            pythonProcess.stdout.on("data", (data) => {
                output += data.toString();
            });

            pythonProcess.stderr.on("data", (data) => {
                errorM += data.toString();
            });

            pythonProcess.on("close", (code) => {
                if (code !== 0) {
                    reject(`Process exited with code ${code}: ${errorM}`);
                } else {
                    try {
                        const json = JSON.parse(output);
                        if (json.status === "success") {
                            resolve(json.result);
                        } else {
                            reject(json.message);
                        }
                    } catch (e) {
                        reject(`Failed to parse output: ${output}`);
                    }
                }
            });
        });
    }
}

// Start Server
const server = createHTTPServer(async () => {
    const mcpServer = new MCPServer({
        name: "textlexiq-mcp-server",
        version: "1.0.0",
    });
    mcpServer.registerService(new TextLexiqTools());
    return mcpServer.getServer();
}, {
    port: 3000,
});

console.log("TextLexiq MCP Server running on port 3000");
