package aggregation

import (
	"testing"

	"github.com/consensys/linea-monorepo/prover/utils"
	"github.com/stretchr/testify/assert"
)

func TestMiniTrees(t *testing.T) {

	cases := []struct {
		MsgHashes []string
		Res       []string
	}{
		{
			MsgHashes: []string{
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x1111111111111111111111111111111111111111111111111111111111111111",
				"0x2222222222222222222222222222222222222222222222222222222222222222",
				"0x3333333333333333333333333333333333333333333333333333333333333333",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
			},
			Res: []string{
				"0x97d2505cd0c868c753353628fbb1aacc52bba62ddebac0536256e1e8560d4f27",
			},
		},
		{
			MsgHashes: []string{
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x1111111111111111111111111111111111111111111111111111111111111111",
				"0x2222222222222222222222222222222222222222222222222222222222222222",
				"0x3333333333333333333333333333333333333333333333333333333333333333",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x5555555555555555555555555555555555555555555555555555555555555555",
				"0x6666666666666666666666666666666666666666666666666666666666666666",
			},
			Res: []string{
				"0x52b5853ebe75cdc639ba9ed15de287bb918b9a0aba00b7aba087de5ee5d0528d",
			},
		},
		{
			MsgHashes: []string{
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x1111111111111111111111111111111111111111111111111111111111111111",
				"0x2222222222222222222222222222222222222222222222222222222222222222",
				"0x3333333333333333333333333333333333333333333333333333333333333333",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x5555555555555555555555555555555555555555555555555555555555555555",
				"0x6666666666666666666666666666666666666666666666666666666666666666",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
			},
			Res: []string{
				"0x52b5853ebe75cdc639ba9ed15de287bb918b9a0aba00b7aba087de5ee5d0528d",
			},
		},
		{
			MsgHashes: []string{
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x1111111111111111111111111111111111111111111111111111111111111111",
				"0x2222222222222222222222222222222222222222222222222222222222222222",
				"0x3333333333333333333333333333333333333333333333333333333333333333",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x5555555555555555555555555555555555555555555555555555555555555555",
				"0x6666666666666666666666666666666666666666666666666666666666666666",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x0000000000000000000000000000000000000000000000000000000000000000",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
				"0x4444444444444444444444444444444444444444444444444444444444444444",
			},
			Res: []string{
				"0x8b25bcdfa0bc56e9e67d3db3c513aa605c8584ac450fb14d62d46cef0fba6f7d",
				"0xcb876e4686e714c06dd52157412e91a490483e9b43e477984c615e6e5dd44b29",
			},
		},
	}

	for i, testcase := range cases {
		res := PackInMiniTrees(testcase.MsgHashes)
		assert.Equal(t, testcase.Res, res, "for case %v", i)
	}
}

func TestL1OffsetBlocks(t *testing.T) {

	testcases := []struct {
		Inps []bool
		Outs string
	}{
		{
			Inps: []bool{true, true, false, false, false},
			Outs: "0x00010002",
		},
		{
			Inps: []bool{false, true, false, false, true, true},
			Outs: "0x000200050006",
		},
	}

	for i, c := range testcases {
		o := PackOffsets(c.Inps)
		oHex := utils.HexEncodeToString(o)
		assert.Equal(t, c.Outs, oHex, "for case %v", i)
	}

}
