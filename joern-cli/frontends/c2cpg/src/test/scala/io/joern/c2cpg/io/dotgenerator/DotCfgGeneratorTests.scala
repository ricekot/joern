package io.joern.c2cpg.io.dotgenerator

import io.joern.c2cpg.testfixtures.C2CpgSuite
import io.shiftleft.semanticcpg.language.*

class DotCfgGeneratorTests extends C2CpgSuite {

  "DotCfgGeneratorTest1" should {
    val cpg = code("""
      |int main(int argc, char **argv) {
      |   int i = 0;
      |   while(i < 10) {
      |     printf("Hello World");
      |     i++;
      |   }
      |   return 0;
      |}
      |""".stripMargin)

    "create a dot graph" in {
      inside(cpg.method.name("main").dotCfg.l) { case List(dotStr) =>
        dotStr should (
          startWith("digraph \"main\" {") and
            include("(&lt;operator&gt;.assignment,i = 0)") and
            endWith("}\n")
        )
      }
    }

    "not contain IDENTIFIER nodes" in {
      inside(cpg.method.name("main").dotCfg.l) { case List(dotStr) =>
        dotStr should not include "IDENTIFIER"
      }
    }

    "contain seven nodes" in {
      inside(cpg.method.name("main").dotCfg.l) { case List(dotStr) =>
        dotStr.split("\n").count(x => x.contains("label")) shouldBe 7
      }
    }

    "contain seven edges" in {
      inside(cpg.method.name("main").dotCfg.l) { case List(dotStr) =>
        dotStr.split("\n").count(x => x.contains("->")) shouldBe 7
      }
    }

  }

  "DotCfgGeneratorTest2" should {
    val cpg = code("""
      |bool a;
      |bool b;
      |
      |bool test() {
      |  return a ? a : b;
      |}""".stripMargin)

    "not contain the same edge more than once" in {
      inside(cpg.method.name("test").dotCfg.l) { case List(dotStr) =>
        val rawEdges   = dotStr.split("\n").filter(x => x.contains("->"))
        val dedupEdges = rawEdges.distinct
        rawEdges.length shouldBe dedupEdges.length
      }
    }

  }

  "DotCfgGeneratorTest3" should {
    val cpg = code("""
        |int example(int a, int b, int c) {
        |  int x = 3;
        |  if(a) { foo(); }
        |  if(b) { foo_2(); }
        |  if (c) { foo_3(); }
        |}
        |""".stripMargin)

    "contain identifiers as conditions" in {
      inside(cpg.method.name("example").dotCfg.l) { case List(dotStr) =>
        dotStr should (
          startWith("digraph \"example\" {") and
            include("<(IDENTIFIER,a,if(a) { foo(); })<SUB>4</SUB>>") and
            include("<(IDENTIFIER,b,if(b) { foo_2(); })<SUB>5</SUB>>") and
            include("<(IDENTIFIER,c,if (c) { foo_3(); })<SUB>6</SUB>>") and
            endWith("}\n")
        )
      }
    }

  }

}
