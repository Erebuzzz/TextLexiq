import { createHTTPServer, Tool } from "@leanmcp/core";
import { z } from "zod";
import { spawn } from "child_process";
import path from "path";

// Define Schema using Zod (standard for MCP tools)
const OcrInputSchema = z.object({
    imagePath: z.string().describe("Absolute path to the image file to process"),
});

class TextLexiqTools {
    @Tool({
        name: "ocr_document",
        description: "Extract text from an image document using TextLexiq engine",
        schema: OcrInputSchema,
    })
    async ocrDocument(input: z.infer<typeof OcrInputSchema>) {
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
const server = createHTTPServer({
    tools: [new TextLexiqTools()],
    port: 3000,
});

console.log("TextLexiq MCP Server running on port 3000");
