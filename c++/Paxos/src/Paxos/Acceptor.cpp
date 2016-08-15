#include "Acceptor.h"


namespace paxos
{


Acceptor::Acceptor(void)
{
	m_maxSerialNum = 0;
	m_lastAcceptValue.serialNum = 0;
	m_lastAcceptValue.value = 0;
}

Acceptor::~Acceptor(void)
{
}

bool Acceptor::Propose(unsigned int serialNum, PROPOSAL &lastAcceptValue)
{
	if ( 0 == serialNum ) return false;
	if ( m_maxSerialNum > serialNum ) return false;
	m_maxSerialNum = serialNum;
	lastAcceptValue = m_lastAcceptValue;

	return true;
}

bool Acceptor::Accept(PROPOSAL &value)
{
	if ( 0 == value.serialNum ) return false;
	if ( m_maxSerialNum > value.serialNum ) return false;
	m_lastAcceptValue = value;
	return true;
}

}
